package org.nuclearfog.twidda.backend.holder;

public class ListHolder {

    public static final long NEW_LIST = -1;

    private long listId;
    private final String title;
    private final String description;
    private final boolean isPublic;


    public ListHolder(String title, String description, boolean isPublic, long listId) {
        this(title, description, isPublic);
        this.listId = listId;
    }


    public ListHolder(String title, String description, boolean isPublic) {
        this.title = title;
        this.description = description;
        this.isPublic = isPublic;
        this.listId = NEW_LIST;
    }

    public long getId() {
        return listId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean exists() {
        return listId != -1;
    }
}