# Gitlet Design Document

**Name**: Janani Sriram

## Classes and Data Structures

### Repository

Holds methods for all commands.

**Instance Variables**
- `File CWD`: current working directory
- `File GITLET_FOLDER`: gitlet folder
- `File COMMIT_DIR`: commit directory
- `File STAGING_AREA`: staging area
- `File BLOBS`: blobs
- `File BRANCHES`: branches
- `File GLOBAL_LOG`: global log
- `File master`: master branch
- `File HEAD`: HEAD pointer

### Commit

Stores commit elements.

**Instance Variables**
- `String message`: message of commit
- `String timestamp`: timestamp of commit
- `String parentUID`: UID (sha1) of commit's parent
- `String UID`: UID (sha1) of commit
- `static HashMap<String, Commit> mapper`: maps UID to commit
- `HashMap<String, String> blobs`: maps file UID to file

### StagingArea

Separates one commit's tracked files from untracked files.

**Instance Variables**
- `ArrayList<File> tracked`: files which should be tracked in next commit
- `ArrayList<File> untracked`: files which should not be tracked in next commit

## Algorithms

### Repository Class

- `init()`: Invoked by `Main.java` when `args[0]` equals `init`.
  1. check if gitlet folder exists (else, throw an error)
  2. create a new commit
  3. create new master and HEAD files
  4. set up branches
  5. write changed objects to corresponding files in .gitlet
- `add()`: Invoked by `Main.java` when `args[0]` equals `add`.
  1. read head commit object and staging area
  2. if commit's blobs contains file and commit's blob's file's UID matches current blob's UID
     1. if stage's untracked files contains the file
        1. remove file from untracked files
        2. write to staging area
     2. exit method
  3. if stage's untracked files contains the file
     1. remove file from untracked files
  4. write to blobs file
  5. put file in tracked files
  6. write to staging area
- `commit()`: Invoked by `Main.java` when `args[0]` equals `commit`.
  1. read head commit object and staging area
  2. clone HEAD commit
  3. modify message and timestamp according to user input
  4. use staging area to modify files tracked by new commit: replace current file contents with contents in staging area
  5. if same fileName (key), different UID (value) --> overwrite blob file with staging area file
  6. write back any new objs made or any modified objs read earlier
- `log()`: Invoked by `Main.java` when `args[0]` equals `log`.
  1. use `StringBuilder` to append commit, UID, and date
- `checkout()`: Invoked by `Main.java` when `args[0]` equals `checkout`.
  1. if input equals file name
     1. if file exists: overwrite file
     2. write contents of new file to old file
  2. if input equals commit ID and file name
     1. pull commit that you want from `COMMIT_DIR`
     2. if file exists: overwrite file
     3. write contents of new file to old file
  3. if input equals branch name
     1. overwrites file versions
     2. delete relevant files
     3. clears staging area
- `remove()`
  1. if staged: unstage file
  2. if tracked in current commit: remove file
- `branch()`
  1. create new branch with input name
  2. write branch to HEAD file
- `status()`
  1. print out status of files and branches (does not involve optional components)

- `getCurrentCommit()`: get latest commit
- `getParentCommit()`: get latest commit's parent
- `getCurrentBranch()`: get latest branch

### Commit Class

- `Commit(String message, String parentUID)`: Class constructor. Sets timestamp, message, parentUID, and UID. Initializes mapper and blobs.
- `getMessage()`: Returns commit message.
- `getTimestamp()`: Returns commit timestamp.
- `getParentUID()`: Returns commit parent's UID.
- `getUID()`: Returns commit UID.
- `getMapper()`: Returns mapper.
- `getBlobs()`: Returns blobs.
- `setMessage()`: Sets commit message.
- `setTimestamp()`: Sets timestamp.

### StagingArea Class

- `StagingArea()`: Class constructor. Initializes `trackedFiles` and `untrackedFiles`.
- `addTracked()`: Add file name and UID to `trackedFiles`.
- `addUntracked()`: Add file name and UID to `untrackedFiles`.
- `clear()`: Reinitialize (clear) `trackedFiles` and `untrackedFiles`.
- `getTrackedFiles()`: Returns `ArrayList` of tracked files.
- `getUntrackedFiles()`: Returns `ArrayList` of untracked files.

## Persistence

**java -ea gitlet.Main testing/test02-basic-checkout.in commit "added wug" wug.txt**

1. Saves current commit using `StagingArea` class and accesses previous commit with `trackedUntracked()`.
2. Writes files to be committed to disk by saving them in `ArrayList<File> tracked`.

**java -ea gitlet.Main testing/test03-basic-log.in log**

1. Saves current commit using `StagingArea` class and accesses previous commit with `trackedUntracked()`.
2. Writes files to be committed to disk by saving them in `ArrayList<File> tracked`.
3. Outputs header, commit date, and commit message.