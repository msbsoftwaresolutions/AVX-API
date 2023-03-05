package net.arvaux.core.cmd;

import java.util.List;

public interface ICompleter {

    List<String> l(GameCommand paramCommand);

}
