
package com.timweng.lib.cmd.unit;

import com.timweng.lib.cmd.Command;

import java.util.Vector;

/**
 * An Action for containing Command in spawn, Command will play simultaneously
 */
public class SpawnCommand extends Command {

    public static enum CompleteType {
        ONE_DONE, ALL_DONE
    }

    public static class SpawnData {
        public Vector<Command> childrenVector;
        public SpawnCommand mSpawnCommand;

        public SpawnData(SpawnCommand spawnCommand, Command[] commands) {
            mSpawnCommand = spawnCommand;
            childrenVector = new Vector<Command>();
            for (int i = 0; i < commands.length; i++) {
                commands[i].setParentId(spawnCommand.getId());
                childrenVector.add(commands[i]);
            }
        }

        public CompleteType getCompleteType() {
            return mSpawnCommand.getCompleteType();
        }

        public SpawnCommand getCommand() {
            return mSpawnCommand;
        }

        public Vector<Command> getChildren() {
            return childrenVector;
        }

        public Command[] genChildrenArray() {
            Command[] array = null;
            synchronized (childrenVector) {
                array = new Command[childrenVector.size()];
                for (int i = 0; i < childrenVector.size(); i++) {
                    array[i] = childrenVector.get(i);
                }
            }
            return array;
        }
    }

    private Command[] mCommands;

    private CompleteType mCompleteType = CompleteType.ONE_DONE;

    public SpawnCommand(Command... commands) {
        mCommands = commands;
    }

    public SpawnData genSpawnData() {
        SpawnData data = new SpawnData(this, mCommands);
        return data;
    }

    public SpawnCommand setCommands(Command... commands) {
        mCommands = commands;
        return this;
    }

    public SpawnCommand setCompleteType(CompleteType type) {
        mCompleteType = type;
        return this;
    }

    public CompleteType getCompleteType() {
        return mCompleteType;
    }
}
