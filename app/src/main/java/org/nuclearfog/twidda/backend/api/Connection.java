package org.nuclearfog.twidda.backend.api;

import org.nuclearfog.twidda.backend.api.twitter.update.MediaUpdate;
import org.nuclearfog.twidda.backend.api.twitter.update.ProfileUpdate;
import org.nuclearfog.twidda.backend.api.twitter.update.TweetUpdate;
import org.nuclearfog.twidda.backend.api.twitter.update.UserlistUpdate;
import org.nuclearfog.twidda.backend.lists.Directmessages;
import org.nuclearfog.twidda.backend.lists.UserLists;
import org.nuclearfog.twidda.backend.lists.Users;
import org.nuclearfog.twidda.model.Location;
import org.nuclearfog.twidda.model.Metrics;
import org.nuclearfog.twidda.model.Relation;
import org.nuclearfog.twidda.model.Trend;
import org.nuclearfog.twidda.model.Tweet;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;

import java.io.InputStream;
import java.util.List;

/**
 * Generic class of a social network connection
 *
 * @author nuclearfog
 */
public interface Connection {
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
	 * @param screen_name screen name of the user
	 * @return user information
	 */
	User showUser(String screen_name) throws ConnectionException;

	/**
	 * search for users matching a search string
	 *
	 * @param search search string
	 * @param page   page of the search results
	 * @return list of users
	 */
	Users searchUsers(String search, long page) throws ConnectionException;

	/**
	 * get users retweeting a tweet
	 *
	 * @param tweetId ID of the tweet
	 * @return user list
	 */
	Users getRetweetingUsers(long tweetId) throws ConnectionException;

	/**
	 * get users liking a tweet
	 *
	 * @param tweetId ID of the tweet
	 * @return user list
	 */
	Users getLikingUsers(long tweetId) throws ConnectionException;

	/**
	 * create a list of users a specified user is following
	 *
	 * @param userId ID of the user
	 * @param cursor cursor value used to parse the list
	 * @return list of users
	 */
	Users getFollowing(long userId, long cursor) throws ConnectionException;

	/**
	 * create a list of users following a specified user
	 *
	 * @param userId ID of the user
	 * @param cursor cursor value used to parse the list
	 * @return list of users
	 */
	Users getFollower(long userId, long cursor) throws ConnectionException;

	/**
	 * create a list of user list members
	 *
	 * @param listId ID of the list
	 * @param cursor cursor value used to parse the list
	 * @return list of users
	 */
	Users getListMember(long listId, long cursor) throws ConnectionException;

	/**
	 * create a list of user list subscriber
	 *
	 * @param listId ID of the list
	 * @param cursor cursor value used to parse the list
	 * @return list of users
	 */
	Users getListSubscriber(long listId, long cursor) throws ConnectionException;

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
	 * @param userId ID of the user
	 * @return relationship information
	 */
	Relation getRelationToUser(long userId) throws ConnectionException;

	/**
	 * follow a specific user
	 *
	 * @param userId ID of the user
	 * @return updated user information
	 */
	User followUser(long userId) throws ConnectionException;

	/**
	 * unfollow a specific user
	 *
	 * @param userId ID of the user
	 * @return updated user information
	 */
	User unfollowUser(long userId) throws ConnectionException;

	/**
	 * block specific user
	 *
	 * @param userId ID of the user
	 * @return updated user information
	 */
	User blockUser(long userId) throws ConnectionException;

	/**
	 * block specific user
	 *
	 * @param screen_name screen name of the user
	 * @return updated user information
	 */
	User blockUser(String screen_name) throws ConnectionException;

	/**
	 * unblock specific user
	 *
	 * @param userId ID of the user
	 * @return updated user information
	 */
	User unblockUser(long userId) throws ConnectionException;

	/**
	 * mute specific user
	 *
	 * @param userId ID of the user
	 * @return updated user information
	 */
	User muteUser(long userId) throws ConnectionException;

	/**
	 * mute specific user
	 *
	 * @param screen_name screen name of the user
	 * @return updated user information
	 */
	User muteUser(String screen_name) throws ConnectionException;

	/**
	 * mute specific user
	 *
	 * @param userId ID of the user
	 * @return updated user information
	 */
	User unmuteUser(long userId) throws ConnectionException;

	/**
	 * search tweets matching a search string
	 *
	 * @param search search string
	 * @param minId  get tweets with ID above the min ID
	 * @param maxId  get tweets with ID under the max ID
	 * @return list of tweets matching the search string
	 */
	List<Tweet> searchTweets(String search, long minId, long maxId) throws ConnectionException;

	/**
	 * get location trends
	 *
	 * @param id world ID
	 * @return trend list
	 */
	List<Trend> getTrends(int id) throws ConnectionException;

	/**
	 * get available locations for trends
	 *
	 * @return list of locations
	 */
	List<Location> getLocations() throws ConnectionException;

	/**
	 * show current user's home timeline
	 *
	 * @param minId get tweets with ID above the min ID
	 * @param maxId get tweets with ID under the max ID
	 * @return list of tweets
	 */
	List<Tweet> getHomeTimeline(long minId, long maxId) throws ConnectionException;

	/**
	 * show current user's home timeline
	 *
	 * @param minId get tweets with ID above the min ID
	 * @param maxId get tweets with ID under the max ID
	 * @return list of tweets
	 */
	List<Tweet> getMentionTimeline(long minId, long maxId) throws ConnectionException;

	/**
	 * show the timeline of an user
	 *
	 * @param userId ID of the user
	 * @param minId  get tweets with ID above the min ID
	 * @param maxId  get tweets with ID under the max ID
	 * @return list of tweets
	 */
	List<Tweet> getUserTimeline(long userId, long minId, long maxId) throws ConnectionException;

	/**
	 * show the timeline of an user
	 *
	 * @param screen_name screen name of the user (without '@')
	 * @param minId       get tweets with ID above the min ID
	 * @param maxId       get tweets with ID under the max ID
	 * @return list of tweets
	 */
	List<Tweet> getUserTimeline(String screen_name, long minId, long maxId) throws ConnectionException;

	/**
	 * show the favorite tweets of an user
	 *
	 * @param userId ID of the user
	 * @param minId  get tweets with ID above the min ID
	 * @param maxId  get tweets with ID under the max ID
	 * @return list of tweets
	 */
	List<Tweet> getUserFavorits(long userId, long minId, long maxId) throws ConnectionException;

	/**
	 * show the favorite tweets of an user
	 *
	 * @param screen_name screen name of the user (without '@')
	 * @param minId       get tweets with ID above the min ID
	 * @param maxId       get tweets with ID under the max ID
	 * @return list of tweets
	 */
	List<Tweet> getUserFavorits(String screen_name, long minId, long maxId) throws ConnectionException;

	/**
	 * return tweets from an user list
	 *
	 * @param listId ID of the list
	 * @param minId  get tweets with ID above the min ID
	 * @param maxId  get tweets with ID under the max ID
	 * @return list of tweets
	 */
	List<Tweet> getUserlistTweets(long listId, long minId, long maxId) throws ConnectionException;

	/**
	 * get replies of a tweet
	 *
	 * @param screen_name screen name of the tweet author
	 * @param tweetId     Id of the tweet
	 * @param minId       get tweets with ID above the min ID
	 * @param maxId       get tweets with ID under the max ID
	 * @return list of tweets
	 */
	List<Tweet> getTweetReplies(String screen_name, long tweetId, long minId, long maxId) throws ConnectionException;

	/**
	 * lookup tweet by ID
	 *
	 * @param tweetId tweet ID
	 * @return tweet information
	 */
	Tweet showTweet(long tweetId) throws ConnectionException;

	/**
	 * favorite specific tweet
	 *
	 * @param tweetId Tweet ID
	 * @return updated tweet
	 */
	Tweet favoriteTweet(long tweetId) throws ConnectionException;

	/**
	 * remove tweet from favorits
	 *
	 * @param tweetId Tweet ID
	 * @return updated tweet
	 */
	Tweet unfavoriteTweet(long tweetId) throws ConnectionException;

	/**
	 * retweet specific tweet
	 *
	 * @param tweetId Tweet ID
	 * @return updated tweet
	 */
	Tweet retweetTweet(long tweetId) throws ConnectionException;

	/**
	 * remove retweet
	 *
	 * @param tweetId ID of the retweeted tweet
	 * @return updated tweet
	 */
	Tweet unretweetTweet(long tweetId) throws ConnectionException;

	/**
	 * hides reply of the own tweet
	 *
	 * @param tweetId ID of the tweet
	 * @param hide    true to hide reply
	 */
	void hideReply(long tweetId, boolean hide) throws ConnectionException;

	/**
	 * remove tweet of the authenticating user
	 *
	 * @param tweetId tweet ID
	 */
	void deleteTweet(long tweetId) throws ConnectionException;

	/**
	 * upload tweet with additional attachment
	 *
	 * @param update tweet update information
	 */
	void uploadTweet(TweetUpdate update, long[] mediaIds) throws ConnectionException;

	/**
	 * create userlist
	 *
	 * @param update Userlist information
	 * @return updated user list
	 */
	UserList createUserlist(UserlistUpdate update) throws ConnectionException;

	/**
	 * update existing userlist
	 *
	 * @param update Userlist update
	 * @return updated user list
	 */
	UserList updateUserlist(UserlistUpdate update) throws ConnectionException;

	/**
	 * return userlist information
	 *
	 * @param listId ID of the list
	 * @return userlist information
	 */
	UserList getUserlist(long listId) throws ConnectionException;

	/**
	 * follow an userlist
	 *
	 * @param listId ID of the list
	 * @return userlist information
	 */
	UserList followUserlist(long listId) throws ConnectionException;

	/**
	 * unfollow an userlist
	 *
	 * @param listId ID of the list
	 * @return userlist information
	 */
	UserList unfollowUserlist(long listId) throws ConnectionException;

	/**
	 * delete an userlist
	 *
	 * @param listId ID of the list
	 * @return removed userlist
	 */
	UserList deleteUserlist(long listId) throws ConnectionException;

	/**
	 * return userlists an user is owning or following
	 *
	 * @param userId      ID of the user
	 * @param screen_name screen name of the user (without '@')
	 * @return list of userlists
	 */
	UserLists getUserListOwnerships(long userId, String screen_name) throws ConnectionException;

	/**
	 * return userlists an user is added to
	 *
	 * @param userId      ID of the user
	 * @param screen_name screen name of the user (without '@')
	 * @param cursor      list cursor
	 * @return list of userlists
	 */
	UserLists getUserListMemberships(long userId, String screen_name, long cursor) throws ConnectionException;

	/**
	 * add user to existing userlist
	 *
	 * @param listId      ID of the list
	 * @param screen_name screen name
	 */
	void addUserToUserlist(long listId, String screen_name) throws ConnectionException;

	/**
	 * remove user from existing userlist
	 *
	 * @param listId      ID of the list
	 * @param screen_name screen name
	 */
	void removeUserFromUserlist(long listId, String screen_name) throws ConnectionException;

	/**
	 * send directmessage to user
	 *
	 * @param userId  ID of the user
	 * @param message message text
	 * @param mediaId ID of uploaded media files or -1 if none
	 */
	void sendDirectmessage(long userId, String message, long mediaId) throws ConnectionException;

	/**
	 * delete directmessage
	 *
	 * @param messageId ID of the message to delete
	 */
	void deleteDirectmessage(long messageId) throws ConnectionException;

	/**
	 * get current user's direct messages
	 *
	 * @param cursor list cursor
	 * @return list of direct messages
	 */
	Directmessages getDirectmessages(String cursor) throws ConnectionException;

	/**
	 * get tweet metrics (views, link clicks, etc.)
	 *
	 * @param tweetId ID of the tweet to get the metrics from
	 * @return tweet metrics
	 */
	Metrics getTweetMetrics(long tweetId) throws ConnectionException;

	/**
	 * upload media file to twitter and generate a media ID
	 *
	 * @param mediaUpdate inputstream with MIME type of the media
	 * @return media ID
	 */
	long uploadMedia(MediaUpdate mediaUpdate) throws ConnectionException;

	/**
	 * download image from twitter
	 *
	 * @param link link to the image
	 * @return image bitmap
	 */
	MediaUpdate downloadImage(String link) throws ConnectionException;

	/**
	 * updates current user's profile
	 *
	 * @param update profile update information
	 * @return updated user information
	 */
	User updateProfile(ProfileUpdate update) throws ConnectionException;

	/**
	 * update current user's profile image
	 *
	 * @param inputStream inputstream of the local image file
	 */
	void updateProfileImage(InputStream inputStream) throws ConnectionException;

	/**
	 * update current user's profile banner image
	 *
	 * @param inputStream inputstream of the local image file
	 */
	void updateBannerImage(InputStream inputStream) throws ConnectionException;

	/**
	 * returns a list of blocked user IDs
	 *
	 * @return list of IDs
	 */
	List<Long> getIdBlocklist() throws ConnectionException;
}