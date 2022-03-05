package gitlet;

import java.io.Serializable;
import java.util.HashMap;

/** StagingArea class.
 * @author Janani Sriram
 * */

public class StagingArea implements Serializable {

    /** Tracked files. */
    private HashMap<String, String> trackedFiles;

    /** Modified files. */
    private HashMap<String, String> modifiedFiles;

    /** Untracked files. */
    private HashMap<String, String> untrackedFiles;

    /** StagingArea constructor. */
    public StagingArea() {
        trackedFiles = new HashMap<>();
        modifiedFiles = new HashMap<>();
        untrackedFiles = new HashMap<>();
    }

    /** Adds tracked files.
     *
     * @param fileName file name
     * @param thisUID UID
     *
     */

    public void addTracked(String fileName, String thisUID) {
        trackedFiles.put(fileName, thisUID);
    }

    /** Adds modified files.
     *
     * @param fileName file name
     * @param thisUID UID
     *
     */
    public void addModified(String fileName, String thisUID) {
        modifiedFiles.put(fileName, thisUID);
    }

    /** Adds untracked files.
     *
     * @param fileName file name
     * @param thisUID UID
     *
     */
    public void addUntracked(String fileName, String thisUID) {
        untrackedFiles.put(fileName, thisUID);
    }

    /** Clears staging area. */

    public void clear() {
        trackedFiles = new HashMap<>();
        modifiedFiles = new HashMap<>();
        untrackedFiles = new HashMap<>();
    }

    /** Gets tracked files.
     *
     * @return trackedFiles
     * */

    public HashMap<String, String> getTrackedFiles() {
        return trackedFiles;
    }

    /** Gets modified files.
     *
     * @return modifiedFiles
     * */

    public HashMap<String, String> getModifiedFiles() {
        return modifiedFiles;
    }

    /** Gets untracked files.
     *
     * @return untrackedFiles
     * */

    public HashMap<String, String> getUntrackedFiles() {
        return untrackedFiles;
    }

}
