package net.arvaux.core.util;

import net.arvaux.core.menu.utils.Colors;
import net.arvaux.core.menu.utils.Message;
import net.arvaux.core.menu.utils.MessageKey;
import net.arvaux.core.menu.utils.PrefixedMessage;

public class RegularMessage extends PrefixedMessage {

    public static final String DEFAULT_FORMAT = Colors.GRAY;

    public RegularMessage(String prefix, MessageKey contents) {
        super(prefix, contents, DEFAULT_FORMAT);
    }

    public RegularMessage(Message prefix, MessageKey contents) {
        super(prefix, contents, DEFAULT_FORMAT);
    }

    public RegularMessage(MessageKey prefix, MessageKey contents) {
        super(prefix, contents, DEFAULT_FORMAT);
    }

    public RegularMessage(MessageKey prefix, String contents) {
        super(prefix, contents);
    }

    public RegularMessage(MessageKey contents) {
        super(contents, DEFAULT_FORMAT);
    }


}
