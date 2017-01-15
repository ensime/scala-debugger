package org.scaladebugger.docs.layouts.partials.common.vendor

import org.scaladebugger.docs.styles.PageStyle

import scalatags.Text.all._

/**
 * Represents a <script ... > containing clipboard.js init code.
 */
object ClipboardJSInit {
  def apply(): Modifier = {
    script(
      s"""
        |var highlightBlocks = document.querySelectorAll('pre code');
        |var copyButtons = [];
        |for (var index in highlightBlocks) {
        |    var hb = highlightBlocks[index];
        |    if (!hb) continue;
        |
        |    var hbParent = hb.parentNode;
        |    if (!hbParent) continue;
        |
        |    hbParent.className += ' ${PageStyle.copyContainer.name}';
        |    var clipboardText = hb.textContent;
        |
        |    var copyButton = document.createElement('button');
        |    copyButton.dataset.clipboardText = clipboardText;
        |    copyButton.className = '${PageStyle.copyButton.name}';
        |    hbParent.insertBefore(copyButton, hb);
        |
        |    var copyButtonText = document.createElement('span');
        |    copyButton.appendChild(copyButtonText);
        |
        |    var copyButtonIcon = document.createElement('i');
        |    copyButtonIcon.className = 'fa fa-clipboard';
        |    copyButton.appendChild(copyButtonIcon);
        |
        |    copyButtons.push(copyButton);
        |}
        |var copyButtonNodeList = (function() {
        |    var tempAttr = 'TEMPCOPYATTR';
        |    copyButtons.forEach(function(button) {
        |        button.setAttribute(tempAttr, '');
        |    });
        |    var buttonNodeList = document.querySelectorAll('[' + tempAttr + ']');
        |    copyButtons.forEach(function(button) {
        |        button.removeAttribute(tempAttr);
        |    });
        |    return buttonNodeList;
        |})();
        |
        |var clipboard = new Clipboard(copyButtonNodeList);
        |console.log('Loaded clipboard for', copyButtonNodeList);
        |
        |var activeTriggers = {};
        |clipboard.on('success', function(e) {
        |    e.trigger.firstChild.textContent = 'Copied! ';
        |
        |    // Prevent old timeout interrupting new session
        |    if (!!activeTriggers[e.trigger]) {
        |        clearTimeout(activeTriggers[e.trigger]);
        |    }
        |
        |    activeTriggers[e.trigger] = setTimeout(function() {
        |        e.trigger.firstChild.textContent = '';
        |        delete activeTriggers[e.trigger];
        |    }, 2000);
        |});
      """.stripMargin)
  }
}
