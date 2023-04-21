package org.nuclearfog.twidda.backend.api;

import org.nuclearfog.twidda.backend.helper.ConnectionConfig;
import org.nuclearfog.twidda.backend.helper.MediaStatus;
import org.nuclearfog.twidda.backend.helper.Messages;
import org.nuclearfog.twidda.backend.helper.ProfileUpdate;
import org.nuclearfog.twidda.backend.helper.StatusUpdate;
import org.nuclearfog.twidda.backend.helper.Statuses;
import org.nuclearfog.twidda.backend.helper.UserListUpdate;
import org.nuclearfog.twidda.backend.helper.UserLists;
import org.nuclearfog.twidda.backend.helper.Users;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.Instance;
import org.nuclearfog.twidda.model.Location;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.Poll;
import org.nuclearfog.twidda.model.Relation;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.Translation;
import org.nuclearfog.twidda.model.Trend;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;

import java.util.List;

/**
 * Generic interface to implement a social network API
 *
 * @author nuclearfog
 */
public interface Connection {

	/**
	 * create authorisation link to open the login page of the social network
	 *
	 * @param connection connection configuration
	 * @return authorisation link to open in a browser
	 */
	String getAuthorisationLink(ConnectionConfig connection) throws ConnectionException;

	/**
	 * login app and get login credentials
	 *
	 * @param connection connection configuration
	 * @param code       verification code to login
	 * @return account information of the created login
	 */
	Account loginApp(ConnectionConfig connection, String code) throws ConnectionException;

	/**
	 * get information about the host server
	 *
	 * @return instance information
	 */
	Instance getInformation() throws ConnectionException;

	/**
	 * lookup user and return user information
	 *
	 * @param id ID of the user
	 * @return user information
	 */
	User showUser(long id) throws ConnectionException;

	/**
	 * lookup user and return user information
	 *
	 * @param name screen name of the user
	 * @return user information
	 */
	User showUser(String name) throws ConnectionException;

	/**
	 * search for users matching a search string
	 *
	 * @param search search string
	 * @param page   page of the search results
	 * @return list of users
	 */
	Users searchUsers(String search, long page) throws ConnectionException;

	/**
	 * get users reposting a status
	 *
	 * @param id ID of the status
	 * @return user list
	 */
	Users getRepostingUsers(long id, long cursor) throws ConnectionException;

	/**
	 * get users liking a status
	 *
	 * @param id ID of the status
	 * @return user list
	 */
	Users getFavoritingUsers(long id, long cursor) throws ConnectionException;

	/**
	 * create a list of users a specified user is following
	 *
	 * @param id     ID of the user
	 * @param cursor cursor value used to parse the list
	 * @return list of users
	 */
	Users getFollowing(long id, long cursor) throws ConnectionException;

	/**
	 * create a list of users following a specified user
	 *
	 * @param id     ID of the user
	 * @param cursor cursor value used to parse the list
	 * @return list of users
	 */
	Users getFollower(long id, long cursor) throws ConnectionException;

	/**
	 * create a list of user list members
	 *
	 * @param id     ID of the list
	 * @param cursor cursor value used to parse the list
	 * @return list of users
	 */
	Users getListMember(long id, long cursor) throws ConnectionException;

	/**
	 * create a list of user list subscriber
	 *
	 * @param id     ID of the list
	 * @param cursor cursor value used to parse the list
	 * @return list of users
	 */
	Users getListSubscriber(long id, long cursor) throws ConnectionException;

	/**
	 * get block list of the current user
	 *
	 * @param cursor cursor value used to parse the list
	 * @return list of users
	 */
	Users getBlockedUsers(long cursor) throws ConnectionException;

	/**
	 * get mute list of the current user
	 *
	 * @param cursor cursor value used to parse the list
	 * @return list of users
	 */
	Users getMutedUsers(long cursor) throws ConnectionException;

	/**
	 * return a list of the 100 recent users requesting a follow
	 *
	 * @param cursor cursor value used to parse the list
	 * @return list of users
	 */
	Users getIncomingFollowRequests(long cursor) throws ConnectionException;

	/**
	 * return a list of the recent 100 users with pending follow requests
	 *
	 * @param cursor cursor value used to parse the list
	 * @return list of users
	 */
	Users getOutgoingFollowRequests(long cursor) throws ConnectionException;

	/**
	 * get relationship information to an user
	 *
	 * @param id ID of the user
	 * @return relationship information
	 */
	Relation getUserRelationship(long id) throws ConnectionException;

	/**
	 * follow a specific user
	 *
	 * @param id ID of the user
	 * @return updated relation to the user
	 */
	Relation followUser(long id) throws ConnectionException;

	/**
	 * unfollow a specific user
	 *
	 * @param id ID of the user
	 * @return updated relation to the user
	 */
	Relation unfollowUser(long id) throws ConnectionException;

	/**
	 * block specific user
	 *
	 * @param id ID of the user
	 * @return updated relation to the user
	 */
	Relation blockUser(long id) throws ConnectionException;

	/**
	 * block specific user
	 *
	 * @param name screen name of the user
	 * @return updated relation to the user
	 */
	Relation blockUser(String name) throws ConnectionException;

	/**
	 * unblock specific user
	 *
	 * @param id ID of the user
	 * @return updated relation to the user
	 */
	Relation unblockUser(long id) throws ConnectionException;

	/**
	 * mute specific user
	 *
	 * @param id ID of the user
	 * @return updated relation to the user
	 */
	Relation muteUser(long id) throws ConnectionException;

	/**
	 * mute specific user
	 *
	 * @param name screen name of the user
	 * @return updated relation to the user
	 */
	Relation muteUser(String name) throws ConnectionException;

	/**
	 * mute specific user
	 *
	 * @param id ID of the user
	 * @return updated relation to the user
	 */
	Relation unmuteUser(long id) throws ConnectionException;

	/**
	 * search statuses matching a search string
	 *
	 * @param search search string
	 * @param minId  get statuses with ID above the min ID
	 * @param maxId  get statuses with ID under the max ID
	 * @return list of statuses matching the search string
	 */
	Statuses searchStatuses(String search, long minId, long maxId) throws ConnectionException;

	/**
	 * get public timeline
	 *
	 * @param minId get statuses with ID above the min ID
	 * @param maxId get statuses with ID under the max ID
	 * @return statuses of the public timeline
	 */
	Statuses getPublicTimeline(long minId, long maxId) throws ConnectionException;

	/**
	 * get location trends
	 *
	 * @return trend list
	 */
	List<Trend> getTrends() throws ConnectionException;

	/**
	 * search hashtags matching search string
	 *
	 * @param search text to search hashtags
	 * @return list of trends (Hashtags)
	 */
	List<Trend> searchHashtags(String search) throws ConnectionException;

	/**
	 * get available locations for trends
	 *
	 * @return list of locations
	 */
	List<Location> getLocations() throws ConnectionException;

	/**
	 * show current user's home timeline
	 *
	 * @param minId get statuses with ID above the min ID
	 * @param maxId get statuses with ID under the max ID
	 * @return list of statuses
	 */
	Statuses getHomeTimeline(long minId, long maxId) throws ConnectionException;

	/**
	 * show the timeline of an user
	 *
	 * @param id    ID of the user
	 * @param minId get statuses with ID above the min ID
	 * @param maxId get statuses with ID under the max ID
	 * @return list of statuses
	 */
	Statuses getUserTimeline(long id, long minId, long maxId) throws ConnectionException;

	/**
	 * show the favorite timeline of an user
	 *
	 * @param id    ID of the user
	 * @param minId get statuses with ID above the min ID
	 * @param maxId get statuses with ID under the max ID
	 * @return list of statuses
	 */
	Statuses getUserFavorits(long id, long minId, long maxId) throws ConnectionException;

	/**
	 * show statuses with bookmarks
	 *
	 * @param minId get statuses with ID above the min ID
	 * @param maxId get statuses with ID under the max ID
	 * @return list of statuses
	 */
	Statuses getUserBookmarks(long minId, long maxId) throws ConnectionException;

	/**
	 * return timeline from an user list
	 *
	 * @param id    ID of the list
	 * @param minId get statuses with ID above the min ID
	 * @param maxId get statuses with ID under the max ID
	 * @return list of statuses
	 */
	Statuses getUserlistStatuses(long id, long minId, long maxId) throws ConnectionException;

	/**
	 * get replies of a status
	 *
	 * @param id     Id of the status
	 * @param minId  get statuses with ID above the min ID
	 * @param maxId  get statuses with ID under the max ID
	 * @param extras additional information like screen name of the status author
	 * @return list of statuses
	 */
	Statuses getStatusReplies(long id, long minId, long maxId, String... extras) throws ConnectionException;

	/**
	 * lookup status by ID
	 *
	 * @param id status ID
	 * @return status information
	 */
	Status showStatus(long id) throws ConnectionException;

	/**
	 * favorite specific status
	 *
	 * @param id status ID
	 * @return updated status
	 */
	Status favoriteStatus(long id) throws ConnectionException;

	/**
	 * remove status from favorits
	 *
	 * @param id status ID
	 * @return updated status
	 */
	Status unfavoriteStatus(long id) throws ConnectionException;

	/**
	 * repost specific status
	 *
	 * @param id status ID
	 * @return updated status
	 */
	Status repostStatus(long id) throws ConnectionException;

	/**
	 * remove repost
	 *
	 * @param id status ID
	 * @return updated status
	 */
	Status removeRepost(long id) throws ConnectionException;

	/**
	 * bookmark status
	 *
	 * @param id status ID
	 * @return updated status
	 */
	Status bookmarkStatus(long id) throws ConnectionException;

	/**
	 * remove status from the bookmarks
	 *
	 * @param id status ID
	 * @return updated status
	 */
	Status removeBookmark(long id) throws ConnectionException;

	/**
	 * mute a status from conversation
	 *
	 * @param id status ID
	 */
	void muteConversation(long id) throws ConnectionException;

	/**
	 * unmute a status from conversation
	 *
	 * @param id status ID
	 */
	void unmuteConversation(long id) throws ConnectionException;

	/**
	 * remove status of the authenticating user
	 *
	 * @param id status ID
	 */
	void deleteStatus(long id) throws ConnectionException;

	/**
	 * upload status with additional attachment
	 *
	 * @param update   status update information
	 * @param mediaIds IDs of the uploaded media files if any
	 * @return uploaded status
	 */
	Status uploadStatus(StatusUpdate update, long[] mediaIds) throws ConnectionException;

	/**
	 * create userlist
	 *
	 * @param update Userlist information
	 * @return updated user list
	 */
	UserList createUserlist(UserListUpdate update) throws ConnectionException;

	/**
	 * update existing userlist
	 *
	 * @param update Userlist update
	 * @return updated user list
	 */
	UserList updateUserlist(UserListUpdate update) throws ConnectionException;

	/**
	 * return userlist information
	 *
	 * @param id ID of the list
	 * @return userlist information
	 */
	UserList getUserlist(long id) throws ConnectionException;

	/**
	 * follow an userlist
	 *
	 * @param id ID of the list
	 * @return userlist information
	 */
	UserList followUserlist(long id) throws ConnectionException;

	/**
	 * unfollow an userlist
	 *
	 * @param id ID of the list
	 * @return userlist information
	 */
	UserList unfollowUserlist(long id) throws ConnectionException;

	/**
	 * delete an userlist
	 *
	 * @param id ID of the list
	 */
	void deleteUserlist(long id) throws ConnectionException;

	/**
	 * return userlists an user is owning or following
	 *
	 * @param id     ID of the user
	 * @param cursor list cursor
	 * @return list of userlists
	 */
	UserLists getUserlistOwnerships(long id, long cursor) throws ConnectionException;

	/**
	 * return userlists an user is added to
	 *
	 * @param id     ID of the user
	 * @param cursor list cursor
	 * @return list of userlists
	 */
	UserLists getUserlistMemberships(long id, long cursor) throws ConnectionException;

	/**
	 * add user to existing userlist
	 *
	 * @param id   ID of the list
	 * @param name screen name
	 */
	void addUserToList(long id, String name) throws ConnectionException;

	/**
	 * remove user from existing userlist
	 *
	 * @param id   ID of the list
	 * @param name screen name
	 */
	void removeUserFromList(long id, String name) throws ConnectionException;

	/**
	 * send directmessage to user
	 *
	 * @param id      ID of the user
	 * @param message message text
	 * @param mediaId ID of uploaded media files or -1 if none
	 */
	void sendDirectmessage(long id, String message, long mediaId) throws ConnectionException;

	/**
	 * delete directmessage
	 *
	 * @param id ID of the message to delete
	 */
	void deleteDirectmessage(long id) throws ConnectionException;

	/**
	 * get current user's direct messages
	 *
	 * @param cursor list cursor
	 * @return list of direct messages
	 */
	Messages getDirectmessages(String cursor) throws ConnectionException;

	/**
	 * get all custom emojis used by the network
	 *
	 * @return array of emojis
	 */
	List<Emoji> getEmojis() throws ConnectionException;

	/**
	 * get poll of a status
	 *
	 * @param id ID of the poll
	 * @return poll instance
	 */
	Poll getPoll(long id) throws ConnectionException;

	/**
	 * send a vote to a poll
	 *
	 * @param poll      poll tovote
	 * @param selection selected poll choices
	 * @return updated poll
	 */
	Poll votePoll(Poll poll, int[] selection) throws ConnectionException;

	/**
	 * returns a list of blocked user IDs
	 *
	 * @return list of IDs
	 */
	List<Long> getIdBlocklist() throws ConnectionException;

	/**
	 * download image
	 *
	 * @param link link to the image
	 * @return image bitmap
	 */
	MediaStatus downloadImage(String link) throws ConnectionException;

	/**
	 * updates current user's profile
	 *
	 * @param update profile update information
	 * @return updated user information
	 */
	User updateProfile(ProfileUpdate update) throws ConnectionException;

	/**
	 * upload media file and generate a media ID
	 *
	 * @param mediaUpdate inputstream with MIME type of the media
	 * @return media ID
	 */
	long uploadMedia(MediaStatus mediaUpdate) throws ConnectionException;

	/**
	 * get notification of the current user
	 *
	 * @param minId minimum ID
	 * @param maxId maximum ID
	 * @return notification list
	 */
	List<Notification> getNotifications(long minId, long maxId) throws ConnectionException;

	/**
	 * get aa single notification by ID
	 *
	 * @param id notification ID
	 * @return notification
	 */
	Notification getNotification(long id) throws ConnectionException;

	/**
	 * dismiss single notification
	 *
	 * @param id notification ID
	 */
	void dismissNotification(long id) throws ConnectionException;

	/**
	 * get the translation of a status
	 *
	 * @param id status ID
	 * @return translation of the status
	 */
	Translation getStatusTranslation(long id) throws ConnectionException;
}