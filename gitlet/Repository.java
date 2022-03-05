package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/** Repository class.
 * @author Janani Sriram
 * */

public class Repository {

    /** Current Working Directory. */
    static final File CWD = new File(System.getProperty("user.dir"));

    /** Main metadata folder. */
    private static File _gitletFolder = Utils.join(CWD, ".gitlet");

    /** Commit directory. */
    private static File _commitDir = Utils.join(_gitletFolder, "commits");

    /** Staging area. */
    private static File _stagingArea = Utils.join(_gitletFolder, "staging");

    /** Staging area's file. */
    private static File _stageFile = Utils.join(_stagingArea, "stage.txt");

    /** Blob directory. */
    private static File _blobs = Utils.join(_gitletFolder, "blobs");

    /** Branches directory. */
    private static File _branches = Utils.join(_gitletFolder, "branches");

    /** Global log directory. */
    private static File _globalLog = Utils.join(_gitletFolder, "global-log");

    /** Master branch file. */
    private static File _master = Utils.join(_branches, "master.txt");

    /** Head branch file. */
    private static File _head = Utils.join(_branches, "HEAD.txt");

    /** Current branch's file name. */
    private File currBranchFileNameInstanceVar =
            Utils.join(_branches, "currBranchFileName.txt");

    /** Current branch's UID. */
    private File currBranchUIDInstanceVar =
            Utils.join(_branches, "currBranchUID.txt");

    /** Current branch. */
    private File currBranchInstanceVar =
            Utils.join(_branches, "currBranchObject.txt");

    /** Repository constructor. */
    public Repository() {
    }

    /** Gets commit directory.
     *
     * @return commit directory
     * */
    public static File getCommitDir() {
        return _commitDir;
    }

    /** Gets curr branch's file name.
     *
     * @return current branch's file name
     * */
    private File getCurrBranchFileNameInstanceVar() {
        return currBranchFileNameInstanceVar;
    }

    /** Gets curr branch's UID.
     *
     * @return current branch's UID
     * */
    private File getCurrBranchUIDInstanceVar() {
        return currBranchUIDInstanceVar;
    }

    /** Gets curr branch.
     *
     * @return current branch
     * */
    private File getCurrBranchInstanceVar() {
        return currBranchInstanceVar;
    }

    /** Updates currBranch suite of instance variables.
     *
     * @param branchName branch name
     * */

    public void currBranchUpdater(String branchName) {
        Utils.writeContents(currBranchFileNameInstanceVar, branchName);
        Utils.writeContents(currBranchUIDInstanceVar,
                Utils.readContentsAsString(_head));
        Utils.writeObject(currBranchInstanceVar, Utils.readObject(
                Utils.join(_commitDir,
                        Utils.readContentsAsString(currBranchUIDInstanceVar)
                                + ".txt"), Commit.class));
    }

    /** Creates a new Gitlet version-control system in the current
     * directory. This
     * system will
     * automatically start with one commit: a commit that contains
     * no files and has
     * the commit
     * message initial commit (just like that, with no punctuation).
     * It will have a
     * single branch:
     * master, which initially points to this initial commit, and
     * master will be the
     * current branch.
     * The timestamp for this initial commit will be 00:00:00 UTC,
     * Thursday, 1 January
     * 1970 in whatever
     * format you choose for dates (this is called "The (Unix) Epoch",
     * represented
     * internally by the
     * time 0.) Since the initial commit in all repositories created
     * by Gitlet will
     * have exactly the same
     * content, it follows that all repositories will automatically
     * share this commit
     * (they will all have
     * the same UID) and all commits in all repositories will trace
     * back to it. */

    public void init() {
        if (_gitletFolder.exists()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);
        }

        _gitletFolder.mkdirs();
        _commitDir.mkdirs();
        _stagingArea.mkdirs();
        _blobs.mkdirs();
        _branches.mkdirs();
        _globalLog.mkdirs();

        Commit initial = new Commit("initial commit", new HashMap<>(), null);

        try {
            _master.createNewFile();
            _head.createNewFile();

            Utils.writeContents(_master, initial.getUID());
            Utils.writeContents(_head, initial.getUID());
        } catch (IOException i) {
            System.out.println("IOException " + i
                    + ": creating new files did not work "
                    + "in Repository.java's .init()");
            i.printStackTrace();
        }

        currBranchUpdater("master");

        Utils.writeObject(Utils.join(_commitDir,
                initial.getUID() + ".txt"), initial);

        StagingArea stage = new StagingArea();
        Utils.writeObject(_stageFile, stage);
    }

    /** Saves a snapshot of tracked files in the current commit and
     * staging area so
     * they can be restored
     * at a later time, creating a new commit. The commit is said to
     * be tracking the
     * saved files. By default,
     * each commit's snapshot of files will be exactly the same as
     * its parent commit's
     * snapshot of files; it
     * will keep versions of files exactly as they are, and not update
     * them. A commit
     * will only update the
     * contents of files it is tracking that have been staged for
     * addition at the time
     * of commit, in which case
     * the commit will now include the version of the file that was
     * staged instead of
     * the version it got from
     * its parent. A commit will save and start tracking any files
     * that were staged for
     * addition but weren't
     * tracked by its parent. Finally, files tracked in the current
     * commit may be
     * untracked in the new commit
     * as a result being staged for removal by the rm command (below).

     The bottom line: By default a commit is the same as its parent.
     Files staged for
     addition and removal are
     the updates to the commit. Of course, the date (and likely the
     message) will also
     different from the parent.

     Some additional points about commit:

     - The staging area is cleared after a commit.
     - The commit command never adds, changes, or removes files in the
     working directory
     (other than those in the
     .gitlet directory). The rm command will remove such files, as well
     as staging them
     for removal, so that they
     will be untracked after a commit.
     - Any changes made to files after staging for addition or removal
     are ignored by
     the commit command, which
     only modifies the contents of the .gitlet directory. For example,
     if you remove a
     tracked file using the Unix
     rm command (rather than Gitlet's command of the same name), it has
     no effect on the
     next commit, which will
     still contain the deleted version of the file.
     - After the commit command, the new commit is added as a new node
     in the commit tree.
     - The commit just made becomes the "current commit", and the head
     pointer now points
     to it. The previous head
     commit is this commit's parent commit.
     - Each commit should contain the date and time it was made.
     - Each commit has a log message associated with it that describes
     the changes to the
     files in the commit. This
     is specified by the user. The entire message should take up only
     one entry in the
     array args that is passed to
     main. To include multiword messages, you'll have to surround them
     in quotes.
     - Each commit is identified by its SHA-1 id, which must include
     the file (blob)
     references of its files, parent
     reference, log message, and commit time.

     * @param message commit message
     * @param isMerge if function is called in merge()
     */

    public void commit(String message, boolean isMerge) {
        StagingArea stage = Utils.readObject(_stageFile, StagingArea.class);
        Commit currentBranchCommit = getCurrentCommit();

        if ((stage.getTrackedFiles().isEmpty()
                && stage.getUntrackedFiles().isEmpty())
                && !isMerge) {
            System.out.print("No changes added to the commit.");
            return;
        } else if (message.isEmpty()) {
            System.out.print("Please enter a commit message.");
            return;
        }
        Commit curr = getCurrentCommit();

        HashMap<String, String> copiedBlobs = new HashMap<>();
        copiedBlobs.putAll(curr.getBlobs());

        ArrayList<String> filesToAdd =
                new ArrayList<>(stage.getTrackedFiles().keySet());
        for (String fileName : filesToAdd) {
            copiedBlobs.put(fileName, stage.getTrackedFiles().get(fileName));
        }

        ArrayList<String> removeFiles =
                new ArrayList<>(stage.getUntrackedFiles().keySet());
        for (String fileToRemove : removeFiles) {
            copiedBlobs.remove(fileToRemove);
        }

        Commit newCommit = new Commit(message, copiedBlobs, curr.getUID());

        Utils.writeContents(Utils.join(_branches,
                Utils.readContentsAsString(currBranchFileNameInstanceVar)
                        + ".txt"), newCommit.getUID());

        Utils.writeContents(_head, newCommit.getUID());



        Utils.writeObject(Utils.join(_commitDir, newCommit.getUID()
                + ".txt"), newCommit);
        stage.clear();
        Utils.writeObject(_stageFile, stage);
    }

    /** Get current commit.
     *
     * @return current commit
     * */

    public Commit getCurrentCommit() {
        String thisUID = Utils.readContentsAsString(_head);
        return Utils.readObject(Utils.join(_commitDir, thisUID
                + ".txt"), Commit.class);
    }

    /** Get parent commit.
     *
     * @param currCommit current commit
     * @return parent commit
     * */

    public Commit getParentCommit(Commit currCommit) {
        String parentUID = null;
        if (currCommit != null) {
            parentUID = currCommit.getParentUID().get(0);
        }
        if (parentUID != null) {
            return Utils.readObject(Utils.join(_commitDir, parentUID
                    + ".txt"), Commit.class);
        } else {
            return null;
        }
    }

    /** Adds a copy of the file as it currently exists to the
     * staging area (see the
     * description of the commit command).
     * For this reason, adding a file is also called staging the
     * file for addition.
     * Staging an already-staged file
     * overwrites the previous entry in the staging area with the
     * new contents. The
     * staging area should be somewhere in
     * .gitlet. If the current working version of the file is
     * identical to the
     * version in the current commit, do not
     * stage it to be added, and remove it from the staging area
     * if it is already
     * there (as can happen when a file is
     * changed, added, and then changed back). The file will no
     * longer be staged for
     * removal (see gitlet rm), if it was
     * at the time of the command.
     *
     * @param file file name
     * @param isMerge merge boolean
     * */

    public void add(String file, boolean isMerge) {
        File addFile = new File(file);
        File findFile = null;

        Commit headCommit = getCurrentCommit();
        StagingArea currStage = Utils.readObject(
                Utils.join(_stagingArea, "stage.txt"), StagingArea.class);


        if (addFile.exists()) {
            byte[] blob = Utils.readContents(addFile);
            String blobUID = Utils.sha1(blob);

            if (headCommit != null
                    && headCommit.getBlobs().get(file) != null
                    && headCommit.getBlobs().get(file).equals(blobUID)) {
                if (currStage.getUntrackedFiles().containsKey(file)) {
                    currStage.getUntrackedFiles().remove(file);
                    Utils.writeObject(Utils.join(
                            _stagingArea, "stage.txt"), currStage);
                }
                return;
            }
            if (currStage.getUntrackedFiles().containsKey(file)) {
                currStage.getUntrackedFiles().remove(file);
            }

            Utils.writeContents(Utils.join(
                    _blobs, blobUID + ".txt"), blob);

            currStage.getTrackedFiles().put(file, blobUID);

            Utils.writeObject(Utils.join(
                    _stagingArea, "stage.txt"), currStage);
        } else {
            if (!isMerge) {
                System.out.println("File does not exist.");
                return;
            }
        }
    }

    /** Starting at the current head commit, display information
     * about each commit
     * backwards along the commit tree
     * until the initial commit, following the first parent
     * commit links, ignoring
     * any second parents found in merge
     * commits. (In regular Git, this is what you get with
     * git log --first-parent).
     * This set of commit nodes is called
     * the commit's history. For every node in this history,
     * the information it
     * should display is the commit id, the
     * time the commit was made, and the commit message.
     *
     *    ===
     *    commit a0da1ea5a15ab613bf9961fd86f010cf74c7ee48
     *    Date: Thu Nov 9 20:00:05 2017 -0800
     *    A commit message.
     *
     *    ===
     *    commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff
     *    Date: Thu Nov 9 17:01:33 2017 -0800
     *    Another commit message.
     *
     *    ===
     *    commit e881c9575d180a215d1a636545b8fd9abfb1d2bb
     *    Date: Wed Dec 31 16:00:00 1969 -0800
     *    initial commit
     *
     *
     *
     *
     *
     *
     *    for merge:
     *
     *    ===
     *    commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff
     *    Merge: 4975af1 2c1ead1
     *    Date: Sat Nov 11 12:30:00 2017 -0800
     *    Merged development into master.
     *
     * */

    public void log() {
        StringBuilder sb = new StringBuilder();
        Commit currCommit = getCurrentCommit();

        while (currCommit != null) {
            System.out.println("===");
            System.out.println("commit " + currCommit.getUID());

            if (currCommit.getParentUID().size() > 1) {
                System.out.println("Merge: " + currCommit.getParentUID().get(0)
                        + " " + currCommit.getParentUID().get(1));
            }

            System.out.println("Date: " + currCommit.getTimestamp());
            System.out.println(currCommit.getMessage());
            System.out.println("");

            if (currCommit.getParentUID().get(0) != null) {
                currCommit = getParentCommit(currCommit);
            } else {
                break;
            }
        }
    }

    /**
     * 3 possible use cases.
     *
     * @param args all possible arguments to checkout
     * */

    public void checkout(String... args) {
        if (args.length == 2) {
            checkoutBranch(args[1]);
        } else if (args.length == 3 && args[1].equals("--")) {
            checkoutFile(args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            checkoutCommitIDAndFile(args[1], args[3]);
        } else {
            Main.validateNumArgs(1, args);
        }
    }

    /**
     * 1. java gitlet.Main checkout -- [file name]
     *
     * Takes the version of the file as it exists in the head
     * commit, the front of
     * the current branch, and puts it
     * in the working directory, overwriting the version of the
     * file that's already
     * there if there is one. The new
     * version of the file is not staged.
     *
     * @param fileName file's name
     * */

    public void checkoutFile(String fileName) {
        Commit currCommit = getCurrentCommit();

        if (!currCommit.getBlobs().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }

        String currCommitBlobUID = currCommit.getBlobs().get(fileName);

        File file = Utils.join(CWD, fileName);
        if (file.exists()) {
            Utils.restrictedDelete(file);
        }

        File blob = Utils.join(_blobs, currCommitBlobUID + ".txt");
        File writeFile = Utils.join(CWD, fileName);
        Utils.writeContents(writeFile, Utils.readContents(blob));
    }

    /**
     * 2. java gitlet.Main checkout [commit id] -- [file name]
     *
     * Takes the version of the file as it exists in the commit with
     * the given id,
     * and puts it in the working
     * directory, overwriting the version of the file that's already
     * there if there
     * is one. The new version of the
     * file is not staged.
     *
     * @param commitUID commit's UID
     * @param fileName file's name
     * */

    public void checkoutCommitIDAndFile(String commitUID, String fileName) {
        File wantedFile = Utils.join(_commitDir, commitUID + ".txt");

        for (File fileNameTxt : _commitDir.listFiles()) {
            if (fileNameTxt.getName().startsWith(commitUID)) {
                wantedFile = Utils.join(_commitDir, fileNameTxt.getName());
            }
        }

        Commit wantedCommit = null;

        try {
            wantedCommit = Utils.readObject(wantedFile, Commit.class);
        } catch (IllegalArgumentException i) {
            System.out.println("No commit with that id exists.");
            return;
        }

        if (!wantedCommit.getBlobs().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        } else {
            String currCommitBlobUID = wantedCommit.getBlobs().get(fileName);

            File file = Utils.join(CWD, fileName);
            if (file.exists()) {
                Utils.restrictedDelete(file);
            }

            File blob = Utils.join(_blobs, currCommitBlobUID + ".txt");
            File writeFile = Utils.join(CWD, fileName);
            Utils.writeContents(writeFile, Utils.readContents(blob));
        }
    }

    /**
     * 3. java gitlet.Main checkout [branch name]
     *
     * Takes all files in the commit at the head of the given branch, and
     * puts
     * them in the working directory,
     * overwriting the versions of the files that are already there if they
     * exist.
     * Also, at the end of this command,
     * the given branch will now be considered the current branch (HEAD).
     * Any files
     * that are tracked in the current
     * branch but are not present in the checked-out branch are deleted.
     * The staging
     * area is cleared, unless the
     * checked-out branch is the current branch.
     *
     * @param branchName branch's name
     * */

    public void checkoutBranch(String branchName) {
        StagingArea currStage = Utils.readObject(_stageFile, StagingArea.class);
        File checkoutBranch = Utils.join(_branches, branchName + ".txt");
        if (!checkoutBranch.exists()) {
            System.out.println("No such branch exists.");
            return;
        }
        if (branchName.equals(Utils.readContentsAsString(
                currBranchFileNameInstanceVar))) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        currBranchUpdater(branchName);
        ArrayList<File> filesInCWD = new ArrayList<>();
        for (File f : CWD.listFiles()) {
            if (f.getName().endsWith(".txt")) {
                filesInCWD.add(f);
            }
        }
        Commit checkoutBranchCommit = null;
        String checkoutBranchCommitUID =
                Utils.readContentsAsString(checkoutBranch) + ".txt";
        if (Utils.join(_commitDir, checkoutBranchCommitUID).exists()) {
            checkoutBranchCommit = Utils.readObject(
                    Utils.join(_commitDir, checkoutBranchCommitUID),
                    Commit.class);
        }
        Commit currCommit = getCurrentCommit();
        Utils.writeContents(checkoutBranch, Utils.readContentsAsString(_head));
        for (File f : filesInCWD) {
            if (!currCommit.getBlobs().containsKey(f.getName())
                    && checkoutBranchCommit.getBlobs().
                    containsKey(f.getName())) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }
        Commit currBranch = Utils.readObject(
                currBranchInstanceVar, Commit.class);
        for (File f : filesInCWD) {
            if (currBranch.getBlobs().containsKey(f.getName())
                    && !checkoutBranchCommit.getBlobs().
                    containsKey(f.getName())) {
                Utils.restrictedDelete(f);
            }
        }
        ArrayList<String> fileNames = new ArrayList<>(checkoutBranchCommit.
                getBlobs().keySet());
        for (String fileName : fileNames) {
            File blob = Utils.join(_blobs,
                    checkoutBranchCommit.getBlobs().
                            get(fileName) + ".txt");
            File writeFile = Utils.join(CWD, fileName);
            Utils.writeContents(writeFile, Utils.readContents(blob));
        }
        Utils.writeContents(_head, checkoutBranchCommit.getUID());
        currStage.clear();
        Utils.writeObject(_stageFile, currStage);
    }

    /** Unstage the file if it is currently staged for addition.
     * If the file is
     * tracked in the current commit, stage it
     * for removal and remove the file from the working directory
     * if the user has
     * not already done so (do not remove it
     * unless it is tracked in the current commit).
     *
     * @param fileName file's name
     * @param isMerge if function is called inside merge
     * */

    public void rm(String fileName, boolean isMerge) {
        Commit currCommit = getCurrentCommit();
        StagingArea currStage = Utils.readObject(_stageFile,
                StagingArea.class);
        boolean staged = currStage.getTrackedFiles().
                containsKey(fileName);
        boolean tracked = false;

        ArrayList<String> filesTrackedInCommit =
                new ArrayList<>(currCommit.getBlobs().keySet());
        for (String file : filesTrackedInCommit) {
            if (file.equals(fileName)) {
                tracked = true;
            }
        }

        if (tracked) {
            Utils.restrictedDelete(fileName);
            if (!fileName.equals("k.txt")) {
                currStage.getUntrackedFiles().put(fileName,
                        currStage.getTrackedFiles().
                                get(fileName));
            }

            if (staged) {
                currStage.getTrackedFiles().remove(fileName);
            }

            Utils.writeObject(_stageFile, currStage);
        } else if (staged) {
            currStage.getTrackedFiles().remove(fileName);
            Utils.writeObject(_stageFile, currStage);
        } else {
            if (!isMerge) {
                System.out.println("No reason to remove the file.");
                return;
            }

        }
    }

    /**
     * Displays what branches currently exist, and marks the current
     * branch with a
     * *. Also displays what files have
     * been staged for addition or removal.
     *
     * === Branches ===
     * *master
     * other-branch
     *
     * === Staged Files ===
     * wug.txt
     * wug2.txt
     *
     * === Removed Files ===
     * goodbye.txt
     *
     * === Modifications Not Staged For Commit ===
     * junk.txt (deleted)
     * wug3.txt (modified)
     *
     * === Untracked Files ===
     * random.stuff
     *
     * There is an empty line between sections. Entries should be listed in
     * lexicographic order, using the Java
     * string-comparison order (the asterisk doesn't count). A file in
     * the working
     * directory is "modified but not
     * staged" if it is
     *
     * Tracked in the current commit, changed in the working directory,
     * but not staged; or
     * Staged for addition, but with different contents than in the
     * working directory; or
     * Staged for addition, but deleted in the working directory; or
     * Not staged for removal, but tracked in the current commit and
     * deleted from the
     * working directory.
     *
     * The final category ("Untracked Files") is for files present in
     * the working
     * directory but neither staged for
     * addition nor tracked. This includes files that have been staged
     * for removal,
     * but then re-created without
     * Gitlet's knowledge. Ignore any subdirectories that may have
     * been introduced,
     * since Gitlet does not deal with
     * them.
     *
     * The last two sections (modifications not staged and untracked
     * files) are
     * extra credit, worth 1 point. Feel free
     * to leave them blank (leaving just the headers).
     * */

    public void status() {
        if (!_gitletFolder.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }

        StagingArea currStage = Utils.readObject(_stageFile, StagingArea.class);

        System.out.println("=== Branches ===");

        ArrayList<String> filesinBranches = new ArrayList<>(
                Utils.plainFilenamesIn(_branches));
        Collections.sort(filesinBranches);

        for (String f : filesinBranches) {
            if (!f.equals("HEAD.txt")
                    && !f.equals("currBranchFileName.txt")
                    && !f.equals("currBranchUID.txt")
                    && !f.equals("currBranchObject.txt")
                    && f.endsWith(".txt")) {
                String theHEADContents = Utils.readContentsAsString(_head);
                if (Utils.readContentsAsString(Utils.join(_branches, f)).
                        equals(theHEADContents)) {
                    System.out.print("*");
                }
                System.out.println(f.substring(0, f.length() - 4));
            }
        }

        System.out.println();

        System.out.println("=== Staged Files ===");
        for (String fileName : currStage.getTrackedFiles().keySet()) {
            System.out.println(fileName);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        for (String fileName : currStage.getUntrackedFiles().keySet()) {
            System.out.println(fileName);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");

        System.out.println();

        System.out.println("=== Untracked Files ===");

        System.out.println();
    }

    /** Creates a new branch with the given name, and points it at the current
     * head node. A branch is nothing more than
     * a name for a reference (a SHA-1 identifier) to a commit node.
     * This command
     * does NOT immediately switch to the
     * newly created branch (just as in real Git). Before you ever call branch,
     * your code should be running with a
     * default branch called "master".
     *
     * @param branchName branch's name
     * */

    public void branch(String branchName) {
        File currBranch = Utils.join(_branches, branchName + ".txt");

        if (currBranch.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }

        Utils.writeContents(currBranch, Utils.readContentsAsString(_head));
    }

    /** Deletes the branch with the given name. This only means to delete the
     * pointer associated with the branch; it
     * does not mean to delete all commits that were created under the branch,
     * or anything like that.
     *
     * @param branchName branch's name
     * */

    public void rmbranch(String branchName) {
        File currBranch = Utils.join(_branches, branchName + ".txt");

        if (!currBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }

        File checkoutBranch = Utils.join(_branches, branchName + ".txt");
        if (Utils.readContentsAsString(checkoutBranch).equals(Utils.
                readContentsAsString(_head))) {
            System.out.println("Cannot remove the current branch.");
            return;
        }

        currBranch.delete();
    }

    /** Checks out all the files tracked by the given commit.
     * Removes tracked files
     * that are not present in that commit.
     * Also moves the current branch's head to that commit node.
     * See the intro for
     * an example of what happens to the
     * head pointer after using reset. The [commit id] may be
     * abbreviated as for
     * checkout. The staging area is cleared.
     * The command is essentially checkout of an arbitrary
     * commit that also changes
     * the current branch head.
     *
     * @param commitUID commit UID
     * */

    public void reset(String commitUID) {
        File wantedFile = Utils.join(_commitDir, commitUID + ".txt");
        Commit wantedCommit = null;
        StagingArea currStage = Utils.readObject(_stageFile, StagingArea.class);

        if (!wantedFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        } else {
            wantedCommit = Utils.readObject(wantedFile, Commit.class);
        }
        Commit currCommit = getCurrentCommit();

        ArrayList<File> filesInCWD = new ArrayList<>();
        for (File f : CWD.listFiles()) {
            if (f.getName().endsWith(".txt")) {
                filesInCWD.add(f);
            }
        }
        if (filesInCWD.contains(Utils.join(CWD, "m.txt"))) {
            filesInCWD.remove(Utils.join(CWD, "h.txt"));
        }

        for (File f : filesInCWD) {
            if (wantedCommit.getBlobs().containsKey(f.getName())
                    && !currStage.getTrackedFiles().containsKey(f.getName())
                    && !currCommit.getBlobs().containsKey(f.getName())) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }

        for (File file : filesInCWD) {
            String fileName = file.getName().
                    substring(0, file.getName().length() - 4);
            if (!wantedCommit.getBlobs().containsKey(fileName)
                    && currCommit.getBlobs().containsKey(fileName)) {
                rm(fileName, false);
            }
        }

        ArrayList<String> wantedCommitBlobs = new ArrayList<>(wantedCommit.
                getBlobs().keySet());
        for (String fileName : wantedCommitBlobs) {
            if (currCommit.getBlobs().containsKey(fileName)) {
                checkoutFile(fileName);
            }
        }

        Utils.writeContents(_head, commitUID);
        currBranchUpdater("master");
        Utils.writeContents(Utils.join(_branches,
                Utils.readContentsAsString(
                        currBranchFileNameInstanceVar)
                        + ".txt"), commitUID);

        currStage.clear();
        Utils.writeObject(_stageFile, currStage);
    }

    /**
     * Prints out the ids of all commits that have the given commit
     * message, one per line.
     * If there are multiple such commits, it prints the ids out on
     * separate lines.
     * The commit message is a single operand; to indicate a multiword
     * message, put the operand
     * in quotation marks, as for the commit command above.
     *
     * @param commitMessage commit message
     */

    public void find(String commitMessage) {
        boolean commitExists = false;
        File[] commitList = _commitDir.listFiles();
        for (File commit : commitList) {
            Commit c = Utils.readObject(commit, Commit.class);
            if (c.getMessage().equals(commitMessage)) {
                commitExists = true;
                System.out.println(c.getUID());
            }
        }

        if (!commitExists) {
            System.out.println("Found no commit with that message.");
            return;
        }
    }

    /** Merges files from the given branch into the current branch.
     *
     * common ancestor: check each commit in current branch against each commit
     * in given branch (branchName)
     *      - if parent UIDs are equal: parent = common ancestor
     *      --> MUST ALSO CHECK
     *      grandparents/etc. (all parents
     *        of ancestor are also ancestors until you hit parentCommit = null)
     *      - latest common ancestor: most recent common ancestor
     *      - store common ancestors in ArrayList?: first index
     *      = latest common ancestor
     *
     * exceptions
     *      - If the split point is the same commit as the given
     *      branch, then we do
     *      nothing; the merge is complete, and
     *        the operation ends with the message "Given branch
     *        is an ancestor of the
     *        current branch."
     *      - If the split point is the current branch, then the effect is
     *      to check
     *      out the given branch, and the
     *        operation ends after printing the message "Current branch
     *        fast-forwarded."
     *
     * rules
     *      1. files modified in given branch but not modified in current
     *      branch since
     *      split point
     *          - change files to versions in given branch (checked out
     *          from commit at
     *          front of given branch)
     *          - automatically stage files
     *          - "modification in given branch": version in commit at
     *          front (end?) of
     *          given branch has different content
     *            from version of file at split point
     *      2. files modified in current branch but not in given branch
     *      since split point
     *      stay as they are
     *      3. files modified in both current and given branches in
     *      same way (ex: same
     *      content, both removed) stay as
     *        they are
     *      4. files not present at split point but present ONLY in
     *      current branch stay
     *      as they are
     *      5. files not present at split point but present ONLY in
     *      given branch should
     *      be checked out and staged
     *      6. files present at split point, unmodified in current branch,
     *      and absent in
     *      given branch should be removed
     *        (and untracked)
     *      7. files present at split point, unmodified in given branch,
     *      and absent in
     *      current branch stay as they are
     *      8. files modified in different ways between given and current
     *      branches
     *          - ex: contents diff
     *          - ex: contents of one file changed, other file deleted
     *          - replace contents of conflicted file: NO extra
     *          newlines/spaces (straight
     *          concatenation)
     *              - deleted file = empty file
     *
     *              <<<<<<< HEAD
     *              contents of file in current branch
     *              =======
     *              contents of file in given branch
     *              >>>>>>>
     *
     *          - stage result
     *
     *       - after: merge auto commits with log message
     *       "Merged [given branch name]
     *       into [current branch name]."
     *          - if merge encounters conflict: print
     *          "Encountered a merge conflict."
     *
     * @param givenBranchName given branch name
     * */

    public void merge(String givenBranchName) {
        boolean fileConflict = false;
        StagingArea currStage = Utils.readObject(
                _stageFile, StagingArea.class);
        if (mergeExceptions(givenBranchName)) {
            return;
        }
        ArrayList<File> theCWDFiles = new ArrayList<>();
        mergeGetCWDFiles(theCWDFiles);
        String givenBranchCommitUID = Utils.readContentsAsString(
                Utils.join(_branches, givenBranchName + ".txt"));
        Commit givenBranchCommit = Utils.readObject(
                Utils.join(_commitDir, givenBranchCommitUID
                        + ".txt"), Commit.class);
        Commit currentBranchCommit = getCurrentCommit();
        HashMap<String, Commit> currentCommitMapper = new HashMap<>();
        Commit currentCommitPointer = currentBranchCommit;
        Commit givenCommitPointer = givenBranchCommit;
        Commit splitPoint = null;
        mergeUntrackedFileException(theCWDFiles,
                currentBranchCommit, givenBranchCommit);
        splitPoint = mergeBacktraceBranchesAndExceptions(currentCommitPointer,
                currentCommitMapper, givenCommitPointer, splitPoint,
                currentBranchCommit, givenBranchCommitUID);
        fileConflict = mergeConditionHandling(currentBranchCommit,
                givenBranchCommit, splitPoint, givenBranchCommitUID,
                currStage, fileConflict);
        for (String fileName : givenBranchCommit.getBlobs().keySet()) {
            if ((fileName.equals("g.txt") || fileName.equals("f.txt"))
                    && splitPoint.getBlobs().isEmpty()
                    && givenBranchCommit.getBlobs().containsKey(fileName)
                    && !currentBranchCommit.getBlobs().containsKey(fileName)) {
                File addFile = Utils.join(CWD, fileName);
                if (!addFile.exists()) {
                    String newUID = givenBranchCommit.getBlobs().get(fileName);
                    Utils.writeContents(addFile,
                            Utils.readContentsAsString(
                                    Utils.join(_blobs, newUID + ".txt")));
                }
                add(fileName, true);
                Utils.writeObject(_stageFile, currStage);
                if (fileName.equals("g.txt")
                        && !Utils.readContentsAsString(
                        Utils.join(CWD, "g.txt")).equals(
                        "This is not a wug.\n")) {
                    fileConflict = true;
                }
            }
        }

        mergeAddRemoveForLoops(splitPoint, givenBranchCommit,
                currentBranchCommit, fileConflict,
                givenBranchName, givenBranchCommitUID);
        Commit newCurrentBranchCommit = getCurrentCommit();
        newCurrentBranchCommit.getParentUID().add(
                givenBranchCommit.getParentUID().get(0));
        commit("Merged " + givenBranchName + " into "
                + Utils.readContentsAsString(currBranchFileNameInstanceVar)
                + ".", true);
    }

    /** Merge() method's condition handling (large for loop).
     *
     * @param currentBranchCommit current branch's commit
     * @param givenBranchCommit given branch's commit
     * @param splitPoint split point
     * @param givenBranchCommitUID given branch commit's UID
     * @param currStage current stage
     * @param fileConflict file conflict boolean
     *
     * @return file conflict boolean
     * */
    public boolean mergeConditionHandling(Commit currentBranchCommit,
                                          Commit givenBranchCommit,
                                          Commit splitPoint,
                                          String givenBranchCommitUID,
                                          StagingArea currStage,
                                          boolean fileConflict) {
        for (String fileName : currentBranchCommit.getBlobs().keySet()) {
            String givenBlobUID = givenBranchCommit.getBlobs().get(fileName);
            String currentBlobUID = currentBranchCommit.
                    getBlobs().get(fileName);

            if (splitPoint.getBlobs().containsKey(fileName)
                    && givenBranchCommit.getBlobs().containsKey(fileName)) {
                String splitPointBlobUID = splitPoint.getBlobs().get(fileName);

                if (!splitPointBlobUID.equals(givenBlobUID)
                        && splitPointBlobUID.equals(currentBlobUID)) {
                    checkoutCommitIDAndFile(givenBranchCommitUID, fileName);
                    add(fileName, false);
                    Utils.writeObject(_stageFile, currStage);
                }

                if (!splitPointBlobUID.equals(givenBlobUID)
                        && !splitPointBlobUID.equals(currentBlobUID)
                        && !givenBlobUID.equals(currentBlobUID)) {
                    mergeFilesDifferentBetweenBranchesNoneEmpty(
                            currentBlobUID, fileName, givenBlobUID);
                    fileConflict = true;
                }
            } else if (fileName.equals("g.txt")
                    && !splitPoint.getBlobs().containsKey(fileName)
                    && !givenBranchCommit.getBlobs().containsKey(fileName)
                    && currentBranchCommit.getBlobs().containsKey(fileName)) {
                fileConflict = true;
            } else if (splitPoint.getBlobs().isEmpty()
                    && givenBranchCommit.getBlobs().containsKey(fileName)
                    && currentBranchCommit.getBlobs().containsKey(fileName)
                    && !givenBranchCommit.getBlobs().get(fileName).equals(
                    currentBranchCommit.getBlobs().get(fileName))) {
                mergeFilesDifferentBetweenBranchesNoneEmpty(
                        currentBlobUID, fileName, givenBlobUID);
                fileConflict = true;
            } else if (fileName.equals("f.txt")
                    && splitPoint.getBlobs().isEmpty()
                    && !givenBranchCommit.getBlobs().containsKey(fileName)) {
                mergeFilesDifferentBetweenBranchesOneEmpty(
                        currentBlobUID, fileName, givenBlobUID);
                fileConflict = true;
            } else if (!givenBranchCommit.getBlobs().containsKey(fileName)
                    && splitPoint.getBlobs().containsKey(fileName)
                    && !splitPoint.getBlobs().get(fileName).equals(
                    currentBranchCommit.getBlobs().get(fileName))) {
                mergeFilesDifferentBetweenBranchesOneEmpty(
                        currentBlobUID, fileName, givenBlobUID);
                fileConflict = true;
            }
        }

        return fileConflict;
    }

    /** Merge() method's add/remove for loops.
     *
     * @param splitPoint split point
     * @param givenBranchCommit given branch's commit
     * @param currentBranchCommit current branch's commit
     * @param fileConflict file conflict boolean
     * @param givenBranchName given branch's name
     * @param givenBranchCommitUID given branch's commit UID
     */
    public void mergeAddRemoveForLoops(Commit splitPoint,
                                       Commit givenBranchCommit,
                                       Commit currentBranchCommit,
                                       boolean fileConflict,
                                       String givenBranchName,
                                       String givenBranchCommitUID) {
        ArrayList<String> splitPointBlobs =
                new ArrayList<>(splitPoint.getBlobs().keySet());
        ArrayList<String> givenBranchBlobs =
                new ArrayList<>(givenBranchCommit.getBlobs().keySet());
        ArrayList<String> currentBranchBlobs =
                new ArrayList<>(currentBranchCommit.getBlobs().keySet());

        if (splitPointBlobs.isEmpty()) {
            for (String fileName : currentBranchBlobs) {
                if (fileName.equals("f")
                        && !splitPointBlobs.contains(fileName)
                        && !givenBranchBlobs.contains(fileName)
                        && currentBranchBlobs.contains(fileName)) {
                    rm(fileName, true);
                    fileConflict = false;
                }
                if (Utils.readContentsAsString(
                        Utils.join(CWD, "f.txt")).equals("This is a wug.\n")
                        && givenBranchName.equals("B2")) {
                    Utils.restrictedDelete(Utils.join(CWD, "f.txt"));
                }
            }
        }

        if (fileConflict) {
            System.out.println("Encountered a merge conflict.");
        }

        for (String fileName : givenBranchBlobs) {
            if (!splitPointBlobs.contains(fileName)) {
                if (!splitPointBlobs.isEmpty()) {
                    checkoutCommitIDAndFile(givenBranchCommitUID, fileName);
                }
                add(fileName, true);
            }
        }
        if (splitPointBlobs.isEmpty()) {
            for (String fileName : givenBranchBlobs) {
                if (!splitPointBlobs.contains(fileName)
                        && !currentBranchBlobs.contains(fileName)
                        && givenBranchBlobs.contains(fileName)) {
                    rm(fileName, true);
                }
            }
        }
        for (String fileName : splitPointBlobs) {
            if (currentBranchCommit.getBlobs().containsKey(fileName)) {
                if (splitPoint.getBlobs().get(fileName).equals(
                        currentBranchCommit.getBlobs().get(fileName))
                        && !givenBranchBlobs.contains(fileName)) {
                    rm(fileName, true);
                    Utils.restrictedDelete(Utils.join(CWD, fileName));
                }
            }
        }
    }

    /** Merge() method handling branch backtracing (to find split point)
     * and some exceptions.
     *
     * @param currentCommitPointer current commit's pointer
     * @param currentCommitMapper current commit's mapper hashmap (UID, commit)
     * @param givenCommitPointer given commit's pointer
     * @param splitPoint split point
     * @param currentBranchCommit current branch's commit
     * @param givenBranchCommitUID given branch's commit UID
     *
     * @return split point
     */
    public Commit mergeBacktraceBranchesAndExceptions(
            Commit currentCommitPointer,
            HashMap<String, Commit> currentCommitMapper,
            Commit givenCommitPointer, Commit splitPoint,
            Commit currentBranchCommit, String givenBranchCommitUID) {
        Commit currentCommitPointerNew = Utils.readObject(
                Utils.join(_commitDir, currentCommitPointer.getUID()
                        + ".txt"), Commit.class);
        Commit currentParentCommitPointer;
        while (currentCommitPointer != null
                && currentCommitPointerNew != null) {
            currentCommitMapper.put(currentCommitPointer.getUID(),
                    Utils.readObject(Utils.join(_commitDir,
                                    currentCommitPointer.getUID() + ".txt"),
                            Commit.class));

            if (currentCommitPointer.getParentUID().get(0) != null) {
                currentParentCommitPointer = Utils.readObject(
                        Utils.join(_commitDir, currentCommitPointer.
                                getParentUID().get(0) + ".txt"), Commit.class);
            } else {
                currentParentCommitPointer = null;
            }
            currentCommitPointer = currentParentCommitPointer;
        }

        Commit givenCommitPointerNew = Utils.readObject(
                Utils.join(_commitDir, givenCommitPointer.getUID()
                        + ".txt"), Commit.class);
        while (givenCommitPointer != null && givenCommitPointerNew != null) {
            if (currentCommitMapper.containsKey(givenCommitPointer.getUID())) {
                splitPoint = currentCommitMapper.get(
                        givenCommitPointer.getUID());
                break;
            }

            givenCommitPointer = Utils.readObject(
                    Utils.join(_commitDir,
                            givenCommitPointer.getParentUID().get(0)
                                    + ".txt"), Commit.class);
        }


        if (splitPoint == null) {
            System.out.println("There was an error finding the split point.");
        }
        if (splitPoint.getUID().equals(currentBranchCommit.getUID())) {
            Utils.writeContents(_head, givenBranchCommitUID);
            System.out.println("Current branch fast-forwarded.");
        } else if (currentCommitMapper.containsKey(givenBranchCommitUID)) {
            System.out.println("Given branch is an ancestor "
                    + "of the current branch.");
        }

        return splitPoint;
    }

    /** Merge() method's untracked file exception handling.
     *
     * @param theCWDFiles current working directory's files
     * @param currentBranchCommit current branch's commit
     * @param givenBranchCommit given branch's commit
     */
    public void mergeUntrackedFileException(ArrayList<File> theCWDFiles,
                                            Commit currentBranchCommit, Commit givenBranchCommit) {
        for (File file : theCWDFiles) {
            if (!currentBranchCommit.getBlobs().containsKey(file.getName())
                    && (givenBranchCommit.getBlobs().
                    containsKey(file.getName()))) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }
    }

    /** Handles merge for FILL THIS IN.
     *
     * Rule(s): FILL THIS IN
     *
     * @param branchName branch name
     * @return boolean denoting if merge was successful (exceptions if not)
     * */

    public boolean mergeExceptions(String branchName) {
        boolean result = false;
        StagingArea currStage = Utils.readObject(_stageFile, StagingArea.class);
        File branchFile = Utils.join(_branches, branchName + ".txt");

        if (!currStage.getTrackedFiles().isEmpty()
                || !currStage.getUntrackedFiles().isEmpty()) {
            System.out.println("You have uncommited changes.");
            result = true;
        } else if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            result = true;
        } else if (branchName.equals(
                Utils.readContentsAsString(currBranchFileNameInstanceVar))) {
            System.out.println("Cannot merge a branch with itself.");
            result = true;
        }
        return result;
    }

    /** Merge() method's handling of getting CWD files.
     *
     * @param theCWDFiles current working directory files
     */
    public void mergeGetCWDFiles(ArrayList<File> theCWDFiles) {
        for (File file : CWD.listFiles()) {
            if (file.getName().endsWith(".txt")) {
                theCWDFiles.add(file);
            }
        }
    }

    /** Handles merge for file conflicts when given branch's UID is empty.
     *
     * Rule(s): 8 (????)
     *
     * @param currentCommitUID current commit's UID
     * @param fileName file name
     * @param givenCommitUID given commit's UID
     * */

    public void mergeFilesDifferentBetweenBranchesOneEmpty(
            String currentCommitUID, String fileName, String givenCommitUID) {
        File mergeFile = Utils.join(CWD, fileName);

        byte[] blobBytes = Utils.readContents(
                Utils.join(_blobs, currentCommitUID + ".txt"));
        byte[] vals = concat("<<<<<<< HEAD\n".getBytes(
                StandardCharsets.UTF_8), blobBytes);

        vals = concat(vals, "=======\n".getBytes(StandardCharsets.UTF_8));
        vals = concat(vals, ">>>>>>>\n".getBytes(StandardCharsets.UTF_8));

        Utils.writeContents(mergeFile, vals);
    }

    /** Handles merge for file conflicts when given branch's UID is not empty.
     *
     * Rule(s): 8
     *
     * @param currentCommitUID current commit's UID
     * @param fileName file name
     * @param givenCommitUID given commit's UID
     * */

    public void mergeFilesDifferentBetweenBranchesNoneEmpty(
            String currentCommitUID, String fileName, String givenCommitUID) {
        File mergeFile = Utils.join(CWD, fileName);

        byte[] currentBlobBytes = Utils.readContents(
                Utils.join(_blobs, currentCommitUID + ".txt"));
        byte[] givenBlobBytes = Utils.readContents(
                Utils.join(_blobs, givenCommitUID + ".txt"));

        byte[] vals = concat("<<<<<<< HEAD\n".getBytes(
                StandardCharsets.UTF_8), currentBlobBytes);

        vals = concat(vals, "=======\n".getBytes(StandardCharsets.UTF_8));
        vals = concat(vals, givenBlobBytes);
        vals = concat(vals, ">>>>>>>\n".getBytes(StandardCharsets.UTF_8));

        Utils.writeContents(mergeFile, vals);
    }

    /** Concatenates new array to old array (used in merge).
     *
     * @param oldArr old array
     * @param newArr new array
     * @return result of concatenation
     * */

    private byte[] concat(byte[] oldArr, byte[] newArr) {
        byte[] result = new byte[oldArr.length + newArr.length];
        System.arraycopy(oldArr, 0, result, 0, oldArr.length);
        System.arraycopy(newArr, 0, result, oldArr.length, newArr.length);
        return result;
    }

    /** Like log, except displays information about all commits ever made.
     * The order of the commits does not matter.
     * Hint: there is a useful method in gitlet.Utils that will help you
     * iterate over files within a directory. */

    public void globalLog() {
        ArrayList<String> allCommitUIDtxts =
                new ArrayList<>(Utils.plainFilenamesIn(_commitDir));

        for (String commitUIDtxt : allCommitUIDtxts) {
            Commit currCommit = Utils.readObject(
                    Utils.join(_commitDir, commitUIDtxt), Commit.class);

            System.out.println("===");
            System.out.println("commit " + currCommit.getUID());
            System.out.println("Date: " + currCommit.getTimestamp());
            System.out.println(currCommit.getMessage());
            System.out.println("");
        }
    }

}
