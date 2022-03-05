package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Janani Sriram
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> ....
     *
     *  ex: java gitlet.Main add hello.txt */

    public static void main(String... args) {
        Repository repo = new Repository();

        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else {
            if (args[0].equals("add")) {
                validateNumArgs(2, args);
                repo.add(args[1], false);
            } else if (args[0].equals("init")) {
                validateNumArgs(1, args);
                repo.init();
            } else if (args[0].equals("commit")) {
                validateNumArgs(2, args);
                repo.commit(args[1], false);
            } else if (args[0].equals("rm")) {
                validateNumArgs(2, args);
                repo.rm(args[1], false);
            } else if (args[0].equals("log")) {
                validateNumArgs(1, args);
                repo.log();
            } else if (args[0].equals("global-log")) {
                validateNumArgs(1, args);
                repo.globalLog();
            } else if (args[0].equals("find")) {
                validateNumArgs(2, args);
                repo.find(args[1]);
            } else if (args[0].equals("status")) {
                validateNumArgs(1, args);
                repo.status();
            } else if (args[0].equals("checkout")) {
                repo.checkout(args);
            } else if (args[0].equals("branch")) {
                validateNumArgs(2, args);
                repo.branch(args[1]);
            } else if (args[0].equals("rm-branch")) {
                validateNumArgs(2, args);
                repo.rmbranch(args[1]);
            } else if (args[0].equals("reset")) {
                validateNumArgs(2, args);
                repo.reset(args[1]);
            } else if (args[0].equals("merge")) {
                validateNumArgs(2, args);
                repo.merge(args[1]);
            } else {
                System.out.println("No command with that name exists.");
                return;
            }
        }
    }

    /** Validates number of arguments to gitlet.
     *
     * @param len length of args
     * @param args arguments
     * @return argument validation
     * */
    public static boolean validateNumArgs(int len, String... args) {
        if (args.length == len) {
            return true;
        }

        System.out.println("Incorrect Operands");
        return false;
    }

}
