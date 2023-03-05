package net.arvaux.core.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

class CompleterComplex implements TabCompleter {

    private Map<String, Entry<Method, Object>> _completers = new HashMap<String, Entry<Method, Object>>();

    public void addCompleter(String label, Method m, Object obj) {
        _completers.put(label, new AbstractMap.SimpleEntry<Method, Object>(m, obj));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        for (int i = args.length; i >= 0; i--) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(label.toLowerCase());
            for (int x = 0; x < i; x++) {
                if (!args[x].equals("") && !args[x].equals(" ")) {
                    buffer.append("." + args[x].toLowerCase());
                }
            }
            String cmdLabel = buffer.toString();
            if (_completers.containsKey(cmdLabel)) {
                Entry<Method, Object> entry = _completers.get(cmdLabel);
                try {
                    return (List<String>) entry.getKey().invoke(entry.getValue(),
                            new GameCommand(sender, command, label, args, cmdLabel.split("\\.").length - 1));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}