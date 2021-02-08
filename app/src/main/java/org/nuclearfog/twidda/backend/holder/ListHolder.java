package org.nuclearfog.twidda.backend.holder;

/**
 * This class stores information about an user list
 *
 * @author nuclearfog
 */
public class ListHolder {

    public static final long NEW_LIST = -1;

    private long listId;
    private final String title;
    private final String description;
    private final boolean isPublic;


    /**
     * @param title       Title of the list
     * @param description short description of the list
     * @param isPublic    true if list should be public
     * @param listId      ID of the list to update or {@link ListHolder#NEW_LIST} to create a new list
     */
    public ListHolder(String title, String description, boolean isPublic, long listId) {
        this(title, description, isPublic);
        this.listId = listId;
    }

    /**
     * @param title       Title of the list
     * @param description short description of the list
     * @param isPublic    true if list should be public
     */
    public ListHolder(String title, String description, boolean isPublic) {
        this.title = title;
        this.description = description;
        this.isPublic = isPublic;
        this.listId = NEW_LIST;
    }

    /**
     * get ID of the list
     *
     * @return list ID
     */
    public long getId() {
        return listId;
    }

    /**
     * get Title of the list
     *
     * @return Title
     */
    public String getTitle() {
        return title;
    }

    /**
     * get short description of the list
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * check if list is visible for everone
     *
     * @return true if list is public
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * check if list exists, so only the information will be updated
     *
     * @return true if list exists
     */
    public boolean exists() {
        return listId != -1;
    }
}