package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/** Commit class.
 * @author Janani Sriram
 * */

public class Commit implements Serializable {

    /** Commit message. */
    private String thismessage;

    /** Commit timestamp. */
    private String thistimestamp;

    /** Commit parentUID. */
    private ArrayList<String> thisparentUID = new ArrayList<>();

    /** Commit UID. */
    private String thisUID;

    /** Commit blobs. */
    private HashMap<String, String> thisblobs;

    /** Commit constructor.
     *
     * @param message commit message
     * @param blobs commit blobs
     * @param parentUID commit parent's UID
     * */
    public Commit(String message, HashMap<String,
            String> blobs, String parentUID) {
        LocalDateTime currentTime = LocalDateTime.now();
        SimpleDateFormat dateTimeFormatter =
                new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        this.thistimestamp = dateTimeFormatter.format(new Date(0));

        this.thismessage = message;
        this.thisparentUID.add(parentUID);
        if (this.thisparentUID.get(0) == null) {
            this.thistimestamp = "Thu Jan 1 00:00:00 1970 -0800";
        } else {
            setTimestamp();
        }
        this.thisUID = Utils.sha1(Utils.serialize(this));
        Utils.writeObject(Utils.join(
                Repository.getCommitDir(), this.thisUID + ".txt"), this);
        this.thisblobs = blobs;
    }

    /** Get commit message.
     *
     * @return commit message
     * */
    public String getMessage() {
        return this.thismessage;
    }

    /** Get commit timestamp.
     *
     * @return commit timestamp
     * */
    public String getTimestamp() {
        return this.thistimestamp;
    }

    /** Get commit parent's UID.
     *
     * @return commit parent's UID
     * */
    public ArrayList<String> getParentUID() {
        return this.thisparentUID;
    }

    /** Get commit UID.
     *
     * @return commit UID
     * */
    public String getUID() {
        return this.thisUID;
    }

    /** Get commit blobs.
     *
     * @return commit blobs
     * */
    public HashMap<String, String> getBlobs() {
        return thisblobs;
    }

    /** Set commit timestamp. */
    public void setTimestamp() {
        LocalDateTime currentTime = LocalDateTime.now();
        SimpleDateFormat dateTimeFormatter =
                new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        this.thistimestamp = dateTimeFormatter.format(new Date(0));
    }
}
