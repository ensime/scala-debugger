package org.scaladebugger.macros.freeze

import scala.reflect.macros.Context
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.reflect.internal.annotations.compileTimeOnly

@compileTimeOnly("enable macro paradise to expand macro annotations")
class Freezable extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro FreezableMacro.impl
}

object FreezableMacro {
  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def isFreezableAnnotation(t: Tree): Boolean = t match {
      case Apply(f, args) => f match {
        case Select(qual, name) =>
          println(s"Freezable: ${qual.getClass.getName} $name")
          false
        case _ => false
      }
      case _ => false
    }


    val (annottee, expandees) = annottees.map(_.tree) match {
      case (classDef: ClassDef) :: rest =>
        // TODO: Should we be using
        // q"$mods trait $tpname[..$tparams] extends { ..$earlydefns } with ..$parents { $self => ..$stats }"
        val q"""
          $mods class $tpname[..$tparams] $ctorMods(...$paramss) extends {
            ..$earlydefns
          } with ..$parents { $self =>
            ..$body
          }
        """ = classDef

        val frozenClass = {
          // TODO: Have an annotation of @Unfreezable that will force
          //       the method to throw an exception when frozen, so we can
          //       use that to fill in exception methods
          //
          //       We leave methods with no annotation alone?
          //
          //       Methods with Freezable will be replaced with the .get
          //       of a Try object and used as part of the Frozen constructor
          val internals = body.map {
            // TODO: Can we include other modifiers/annotations still?
            case q"@Freezable $mods def $tname[..$tparams](...$paramss): $tpt = $expr" =>
              if (paramss.nonEmpty) c.abort(
                c.enclosingPosition,
                s"Unable to freeze $tname! Must have zero arguments!"
              )

              // TODO: Should/do we need to mark this as overriden since
              //       that's what we are doing to the parent class?
              q"def $tname[..$tparams](...$paramss): $tpt = $tname(...$args)"
            case q"$mods def $tname[..$tparams](...$paramss): $tpt = $expr" =>
              // TODO: Should this only throw an exception if there is no
              //       body? In other words, just fill abstract methods with
              //       the exception
              q"def $tname[..$tparams](...$paramss): $tpt = throw new Exception"
            case t => t
          }

          // TODO: scala.util.Try is not marked as serializable in 2.10/2.11;
          //       happens in 2.12; Success/Failure are case classes and a
          //       throwable in Java is already serializable.
          //
          //       Make a wrapper for this that implements serializable?
          //
          //       Any method not marked Freezable needs to have its
          //       implementation changed to throw a NotFrozenException
          //
          //       Any method marked Freezable needs to use the .get method
          //       from the scala.util.Try equivalent as the implementation
          //
          //       Constructor to class needs to take scala.util.Try equivalent
          //       as input for each method marked Freezable
          q"""
            class Frozen private(...$paramss) extends $tpname with java.io.Serializable { $self =>
                ..$internals
            }
          """
        }

        val freezeMethod = {
          // TODO: Arguments need to be calls to scala.util.Try equivalent
          //       with input as call to parent class' method name
          val args = List().map(t => q"""scala.util.Try.apply($t)""")
          q"""
            def freeze(): ${frozenClass.name} = new ${frozenClass.name} {
              ..$args
            }
          """
        }

        val newContent = List(frozenClass, freezeMethod)

        // Generate a new class containing the helper methods and
        // inner "Frozen" class representation
        val newClassDef = q"""
          $mods class $tpname[..$tparams] $ctorMods(...$paramss) extends {
            ..$earlydefns
          } with ..$parents { $self =>
            ..$body
            ..$newContent
          }
        """

        (EmptyTree, newClassDef +: rest)
      /*case (d: ClassDef) :: rest =>
        println(q"""
          @Freezable private def potato: Int = 3
        """)
        (EmptyTree, d +: rest)*/
      case (d: DefDef) :: rest =>
        if (d.tparams.nonEmpty) c.abort(
          c.enclosingPosition,
          "Freezable cannot be placed on a method with arguments!"
        )

        (EmptyTree, d +: rest)
      case (d: ValDef) :: rest => d.tpt match {
        // TODO: What types can we support here?
        case _: Constant => (EmptyTree, d +: rest)
        case _ => c.abort(
          c.enclosingPosition,
          "Freezable can only be placed on vals of constants!"
        )
      }
      case _ =>
        c.abort(
          c.enclosingPosition,
          "Freezable can only be placed on traits/classes, vals, and methods!"
        )
    }
    //println((annottee, expandees))
    val outputs = expandees
    c.Expr[Any](Block(outputs, Literal(Constant(()))))
  }
}
