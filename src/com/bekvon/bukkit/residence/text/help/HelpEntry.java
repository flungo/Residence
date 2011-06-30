/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.text.help;

import com.bekvon.bukkit.residence.Residence;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.util.config.Configuration;

/**
 *
 * @author Administrator
 */
public class HelpEntry {
    protected String name;
    protected String desc;
    protected String[] lines;
    protected List<HelpEntry> subentrys;
    protected static int linesPerPage = 6;

    public HelpEntry(String entryname)
    {
        name = entryname;
        subentrys = new ArrayList<HelpEntry>();
        lines = new String[0];
    }

    public String getName() {
        if(name==null)
            return "";
        return name;
    }

    public void setName(String inname)
    {
        name = inname;
    }

    public void setDescription(String description)
    {
        desc = description;
    }
    public String getDescription()
    {
        if(desc==null)
            return "";
        return desc;
    }

    public void printHelp(CommandSender sender, int page) {
        List<String> helplines = this.getHelpData();
        int pagecount = (int) Math.ceil((double)helplines.size() / (double)linesPerPage);
        if (page > pagecount || page < 1) {
            sender.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidHelp"));
            return;
        }
        sender.sendMessage("§a"+Residence.getLanguage().getPhrase("HelpPageHeader","§e" + name + "§a.§e" + page + "§a.§e" + pagecount + "§a"));
        sender.sendMessage("§6"+Residence.getLanguage().getPhrase("Description")+" §c" + desc);
        int start = linesPerPage * (page - 1);
        int end = start + linesPerPage;
        for (int i = start; i < end; i++) {
            if (helplines.size() > i) {
                sender.sendMessage("§9"+helplines.get(i));
            }
        }
    }

    public void printHelp(CommandSender sender, int page, String path)
    {
        HelpEntry subEntry = this.getSubEntry(path);
        if(subEntry!=null)
        {
            subEntry.printHelp(sender, page);
        }
        else
        {
            sender.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidHelp"));
        }
    }

    private List<String> getHelpData()
    {
        List<String> helplines = new ArrayList<String>();
        helplines.addAll(Arrays.asList(lines));
        if(subentrys.size()>0)
            helplines.add("§d---"+Residence.getLanguage().getPhrase("SubCommands")+"---");
        for(HelpEntry entry : subentrys)
        {
            helplines.add("§a"+entry.getName() + "§e - " + entry.getDescription());
        }
        return helplines;
    }

    public boolean containesEntry(String name)
    {
        return this.getSubEntry(name)!=null;
    }

    public HelpEntry getSubEntry(String name)
    {
        String[] split = name.split("\\.");
        HelpEntry entry = this;
        for(String entryname : split)
        {
            entry = entry.findSubEntry(entryname);
            if(entry == null)
                return null;
        }
        return entry;
    }

    private HelpEntry findSubEntry(String name)
    {
        for(HelpEntry entry : subentrys)
        {
            if(entry.getName().equalsIgnoreCase(name))
                return entry;
        }
        return null;
    }

    public void addSubEntry(HelpEntry entry)
    {
        if(!subentrys.contains(entry))
        {
            subentrys.add(entry);
        }
    }

    public void removeSubEntry(HelpEntry entry)
    {
        if(subentrys.contains(entry))
        {
            subentrys.remove(entry);
        }
    }

    public int getSubEntryCount()
    {
        return subentrys.size();
    }

    public static HelpEntry parseHelp(Configuration node, String key)
    {
        String split[] = key.split("\\.");
        String thisname = split[split.length-1];
        HelpEntry entry = new HelpEntry(thisname);
        List<String> keys = node.getKeys(key);
        if(keys!=null)
        {
            if(keys.contains("Info"))
            {
                List<String> stringList = node.getStringList(key + ".Info", null);
                if(stringList != null)
                {
                    entry.lines = new String[stringList.size()];
                    for(int i = 0; i < stringList.size(); i++)
                    {
                        entry.lines[i] = stringList.get(i);
                    }
                }
            }
            if(keys.contains("Description"))
            {
                entry.desc = node.getString(key + ".Description");
            }
            if(keys.contains("SubCommands"))
            {
                List<String> subcommandkeys = node.getKeys(key + ".SubCommands");
                for(String subkey : subcommandkeys)
                {
                    entry.subentrys.add(HelpEntry.parseHelp(node, key+".SubCommands."+subkey));
                }
            }
        }
        return entry;
    }

}
