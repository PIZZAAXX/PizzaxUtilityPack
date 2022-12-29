package pizzaaxx.pizzaxutilitypack.Discord;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandComparison {

    private static class SubcommandGroup {

        private final String name;

        private final String description;
        private final List<Subcommand> subcommands;
        private SubcommandGroup(String name, String description) {
            this.name = name;
            this.description = description;
            this.subcommands = new ArrayList<>();
        }

        public void addSubcommand(String name, String description) {
            subcommands.add(
                    new Subcommand(
                            name,
                            description
                    )
            );
        }

        public void addSubcommand(Subcommand subcommand) {
            subcommands.add(subcommand);
        }

        public List<Subcommand> getSubcommands() {
            return subcommands;
        }
    }
    private static class Subcommand {

        private final String name;

        private final String description;
        private final List<Option> options;
        private Subcommand(String name, String description) {
            this.name = name;
            this.description = description;
            this.options = new ArrayList<>();
        }

        public void addOption(OptionType type, String name, String description) {
            options.add(
                    new Option(
                            type,
                            name,
                            description
                    )
            );
        }

        public void addOption(Option option) {
            options.add(option);
        }

        public List<Option> getOptions() {
            return options;
        }
    }
    private static class Option {

        private final OptionType type;

        private final String name;
        private final String description;
        private Option(OptionType type, String name, String description) {
            this.type = type;
            this.name = name;
            this.description = description;
        }

    }
    private final String name;

    private final String description;
    private final List<SubcommandGroup> subcommandGroups;
    private final List<Subcommand> subcommands;
    private final List<Option> options;

    public CommandComparison(String name, String description) {
        this.name = name;
        this.description = description;
        this.subcommandGroups = new ArrayList<>();
        this.subcommands = new ArrayList<>();
        this.options = new ArrayList<>();
    }

    public void addSubcommandGroup(String name, String description) {
        subcommandGroups.add(
                new SubcommandGroup(
                        name,
                        description
                )
        );
    }

    public void addSubcommandGroup(SubcommandGroup subcommandGroup) {
        subcommandGroups.add(subcommandGroup);
    }

    public void addSubcommand(String name, String description) {
        subcommands.add(
                new Subcommand(
                        name,
                        description
                )
        );
    }

    public void addSubcommand(Subcommand subcommand) {
        subcommands.add(subcommand);
    }

    public void addOption(OptionType type, String name, String description) {
        options.add(
                new Option(
                        type,
                        name,
                        description
                )
        );
    }

    public void addOption(Option option) {
        options.add(option);
    }

    public boolean compare(@NotNull Command command) {
        if (!name.equals(command.getName()) || !description.equals(command.getDescription())) {
            return false;
        }

        boolean matchesAllSubcommandGroups = true;
        for (Command.SubcommandGroup subcommandGroup1 : command.getSubcommandGroups()) {
            boolean matchesSubcommandGroup = false;
            for (SubcommandGroup subcommandGroup2 : this.subcommandGroups) {
                boolean matchesAllSubcommands = true;
                for (Command.Subcommand subcommand1 : subcommandGroup1.getSubcommands()) {
                    boolean matchesSubcommand = false;
                    for (Subcommand subcommand2 : subcommandGroup2.getSubcommands()) {
                        boolean matchesAllOptions = true;
                        for (Command.Option option1 : subcommand1.getOptions()) {
                            boolean matchesOption = false;
                            for (Option option2 : subcommand2.getOptions()) {
                                if (option1.getName().equals(option2.name) && option1.getDescription().equals(option2.description) && option1.getType() == option2.type) {
                                    matchesOption = true;
                                    break;
                                }
                            }
                            if (!matchesOption) {
                                matchesAllOptions = false;
                                break;
                            }
                        }
                        matchesSubcommand = matchesAllOptions;
                    }
                    if (!matchesSubcommand) {
                        matchesAllSubcommands = false;
                        break;
                    }
                }
                matchesSubcommandGroup = matchesAllSubcommands;
            }
            if (!matchesSubcommandGroup) {
                matchesAllSubcommandGroups = false;
                break;
            }
        }
        if (!matchesAllSubcommandGroups) {
            return false;
        }

        boolean matchesAllSubcommands = true;
        for (Command.Subcommand subcommand1 : command.getSubcommands()) {
            boolean matchesSubcommand = false;
            for (Subcommand subcommand2 : subcommands) {
                boolean matchesAllOptions = true;
                for (Command.Option option1 : subcommand1.getOptions()) {
                    boolean matchesOption = false;
                    for (Option option2 : subcommand2.getOptions()) {
                        if (option1.getName().equals(option2.name) && option1.getDescription().equals(option2.description) && option1.getType() == option2.type) {
                            matchesOption = true;
                            break;
                        }
                    }
                    if (!matchesOption) {
                        matchesAllOptions = false;
                        break;
                    }
                }
                matchesSubcommand = matchesAllOptions;
            }
            if (!matchesSubcommand) {
                matchesAllSubcommands = false;
                break;
            }
        }
        if (!matchesAllSubcommands){
            return false;
        }

        boolean matchesAllOptions = true;
        for (Command.Option option1 : command.getOptions()) {
            boolean matchesOption = false;
            for (Option option2 : options) {
                if (option1.getName().equals(option2.name) && option1.getDescription().equals(option2.description) && option1.getType() == option2.type) {
                    matchesOption = true;
                    break;
                }
            }
            if (!matchesOption) {
                matchesAllOptions = false;
                break;
            }
        }
        return matchesAllOptions;
    }

}
