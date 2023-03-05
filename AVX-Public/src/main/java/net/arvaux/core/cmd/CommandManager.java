package net.arvaux.core.cmd;

import net.arvaux.core.Main;
import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.group.Group;
import net.arvaux.core.util.M;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

public class CommandManager implements CommandExecutor {

    private Map<String, Entry<Method, Object>> _field_a = new HashMap<String, Entry<Method, Object>>();
    private CommandMap _field_b;

    public CommandManager() {
        if (Main.getInstance().getServer().getPluginManager() instanceof SimplePluginManager) {
            SimplePluginManager manager = (SimplePluginManager) Main.getInstance().getServer().getPluginManager();
            try {
                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);
                _field_b = (CommandMap) field.get(manager);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    private void func_a(GameCommand args) {
        args.getSender().sendMessage(M.COMMAND + "§c" + args.getLabel() + "§c couldn't be handled by GameCommand.");
    }

    public void func_C(Command command, String label, Method m, ICommand obj) {
        _field_a.put(label.toLowerCase(), new AbstractMap.SimpleEntry<Method, Object>(m, obj));
        _field_a.put(Main.getInstance().getName() + ':' + label.toLowerCase(),
                new AbstractMap.SimpleEntry<Method, Object>(m, obj));
        String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();
        if (_field_b.getCommand(cmdLabel) == null) {
            org.bukkit.command.Command cmd = new CommandComplex(cmdLabel, this, Main.getInstance());
            _field_b.register(Main.getInstance().getName(), cmd);
        }
        if (!command.permission().getPerm().equalsIgnoreCase("") && cmdLabel == label) {
            _field_b.getCommand(cmdLabel).setPermission(command.permission().getPerm());
            // TODO check powerlevel
            _field_b.getCommand(cmdLabel).setPermissionMessage(M.COMMAND_NO_PERMS);
        }
        if (!command.description().equalsIgnoreCase("") && cmdLabel == label) {
            _field_b.getCommand(cmdLabel).setDescription(command.description());
        }
        if (!command.usage().equalsIgnoreCase("") && cmdLabel == label) {
            _field_b.getCommand(cmdLabel).setUsage(command.usage());
        }
    }

    public void registerCommands(ICommand obj) {
        for (Method m : obj.getClass().getMethods()) {
            if (m.getAnnotation(Command.class) != null) {
                Command command = m.getAnnotation(Command.class);
                if (command.permission() == Group.REGULAR) {
                    Arrays.stream(command.aliases()).forEach(aliases -> Main.COMMAND_LIST.add("/" + aliases));
                    Main.COMMAND_LIST.add("/" + command.name());
                }
                if (m.getParameterTypes().length > 1 || m.getParameterTypes()[0] != GameCommand.class) {
                    System.out.println("Unable to register command " + m.getName() + ". Unexpected method arguments");
                    continue;
                }
                func_C(command, command.name(), m, obj);
                for (String alias : command.aliases()) {
                    func_C(command, alias, m, obj);
                }
            } else if (m.getAnnotation(TabCompleter.class) != null) {
                TabCompleter comp = m.getAnnotation(TabCompleter.class);
                if (m.getParameterTypes().length > 1 || m.getParameterTypes().length == 0
                        || m.getParameterTypes()[0] != GameCommand.class) {
                    System.out.println(
                            "Unable to register tab completer " + m.getName() + ". Unexpected method arguments");
                    continue;
                }
                if (m.getReturnType() != List.class) {
                    System.out.println("Unable to register tab completer " + m.getName() + ". Unexpected return type");
                    continue;
                }
                func_T(comp.name(), m, obj);
                for (String alias : comp.aliases()) {
                    func_T(alias, m, obj);
                }
            }
        }
    }

    public void func_T(String label, Method m, Object obj) {
        String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();
        if (_field_b.getCommand(cmdLabel) == null) {
            org.bukkit.command.Command command = new CommandComplex(cmdLabel, this, Main.getInstance());
            _field_b.register(Main.getInstance().getName(), command);
        }
        if (_field_b.getCommand(cmdLabel) instanceof CommandComplex) {
            CommandComplex command = (CommandComplex) _field_b.getCommand(cmdLabel);
            if (command._completer == null) {
                command._completer = new CompleterComplex();
            }
            command._completer.addCompleter(label, m, obj);
        } else if (_field_b.getCommand(cmdLabel) instanceof PluginCommand) {
            try {
                Object command = _field_b.getCommand(cmdLabel);
                Field field = command.getClass().getDeclaredField("completer");
                field.setAccessible(true);
                if (field.get(command) == null) {
                    CompleterComplex completer = new CompleterComplex();
                    completer.addCompleter(label, m, obj);
                    field.set(command, completer);
                } else if (field.get(command) instanceof CompleterComplex) {
                    CompleterComplex completer = (CompleterComplex) field.get(command);
                    completer.addCompleter(label, m, obj);
                } else {
                    System.out.println("Unable to register tab completer " + m.getName()
                            + ". F tab completer is already registered for that command!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void H() {
        Set<HelpTopic> help = new TreeSet<HelpTopic>(HelpTopicComparator.helpTopicComparatorInstance());
        for (String s : _field_a.keySet()) {
            if (!s.contains(".")) {
                org.bukkit.command.Command cmd = _field_b.getCommand(s);
                HelpTopic topic = new GenericCommandHelpTopic(cmd);
                help.add(topic);
            }
        }
        IndexHelpTopic topic = new IndexHelpTopic(Main.getInstance().getName(),
                "All commands for " + Main.getInstance().getName(), null, help,
                "Below is a list of all " + Main.getInstance().getName() + " commands:");
        Bukkit.getServer().getHelpMap().addTopic(topic);
    }

    public boolean func_b(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        for (int i = args.length; i >= 0; i--) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(label.toLowerCase());
            for (int x = 0; x < i; x++) {
                buffer.append("." + args[x].toLowerCase());
            }
            String cmdLabel = buffer.toString();
            if (_field_a.containsKey(cmdLabel)) {
                Method method = _field_a.get(cmdLabel).getKey();
                Object methodObject = _field_a.get(cmdLabel).getValue();
                Command command = method.getAnnotation(Command.class);

                if (sender instanceof Player) {
                    GamePlayer player = new GamePlayer(((Player) sender).getUniqueId());
                    if (player.getGroup().getPowerLevel() < command.permission().getPowerLevel()) {
                        sender.sendMessage(M.COMMAND_NO_PERMS);
                        return true;
                    }
                }
                if (command.inGameOnly() && !(sender instanceof Player)) {
                    sender.sendMessage(M.COMMAND + "§cYou can only preform this command within the game");
                    return true;
                }
                try {
                    method.invoke(methodObject,
                            new GameCommand(sender, cmd, label, args, cmdLabel.split("\\.").length - 1));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        func_a(new GameCommand(sender, cmd, label, args, 0));
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        return func_b(sender, cmd, label, args);
    }

}
