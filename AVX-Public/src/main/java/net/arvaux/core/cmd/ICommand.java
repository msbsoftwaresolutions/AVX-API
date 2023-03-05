package net.arvaux.core.cmd;

import java.io.IOException;
import java.sql.SQLException;

public interface ICommand {

    boolean k(GameCommand paramCommand) throws IOException, SQLException;

}
