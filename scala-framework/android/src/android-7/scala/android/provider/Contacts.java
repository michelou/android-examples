/*                     __                                               *\
**     ________ ___   / /  ___     Scala Android                        **
**    / __/ __// _ | / /  / _ |    (c) 2009-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */


package scala.android.provider;

/*
 * This helper class is a workaround for accessing Java static constants from
 * Scala code when whose constants are defined in Java static inner interfaces.
 *
 * @author Stephane Micheloud
 * @version 1.0
 */
//public final class android.provider.Contacts
@Deprecated
public final class Contacts {

    // public static final class android.provider.Contacts.ContactMethods
    @Deprecated
    public static final class ContactMethods {

        /** @since API level 1 */
        public static final String CONTENT_EMAIL_ITEM_TYPE =
            android.provider.Contacts.ContactMethods.CONTENT_EMAIL_ITEM_TYPE;
        /** @since API level 1 */
        public static final String CONTENT_EMAIL_TYPE =
            android.provider.Contacts.ContactMethods.CONTENT_EMAIL_TYPE;
        /** @since API level 1 */
        public static final android.net.Uri CONTENT_EMAIL_URI =
            android.provider.Contacts.ContactMethods.CONTENT_EMAIL_URI;
        /** @since API level 1 */
        public static final String CONTENT_IM_ITEM_TYPE =
            android.provider.Contacts.ContactMethods.CONTENT_IM_ITEM_TYPE;
        /** @since API level 1 */
        public static final String CONTENT_POSTAL_ITEM_TYPE =
            android.provider.Contacts.ContactMethods.CONTENT_POSTAL_ITEM_TYPE;
        /** @since API level 1 */
        public static final String CONTENT_POSTAL_TYPE =
            android.provider.Contacts.ContactMethods.CONTENT_POSTAL_TYPE;
        /** @since API level 1 */
        public static final String CONTENT_TYPE =
            android.provider.Contacts.ContactMethods.CONTENT_TYPE;
        /** @since API level 1 */
        public static final android.net.Uri CONTENT_URI =
            android.provider.Contacts.ContactMethods.CONTENT_URI;
        /** @since API level 1 */
        public static final String DEFAULT_SORT_ORDER =
            android.provider.Contacts.ContactMethods.DEFAULT_SORT_ORDER;
        /** @since API level 1 */
        public static final String PERSON_ID =
            android.provider.Contacts.ContactMethods.PERSON_ID;
        /** @since API level 1 */
        public static final String POSTAL_LOCATION_LATITUDE =
            android.provider.Contacts.ContactMethods.POSTAL_LOCATION_LATITUDE;
        /** @since API level 1 */
        public static final String POSTAL_LOCATION_LONGITUDE =
            android.provider.Contacts.ContactMethods.POSTAL_LOCATION_LONGITUDE;
        /** @since API level 1 */
        public static final int PROTOCOL_AIM =
            android.provider.Contacts.ContactMethods.PROTOCOL_AIM;
        /** @since API level 1 */
        public static final int PROTOCOL_GOOGLE_TALK =
            android.provider.Contacts.ContactMethods.PROTOCOL_GOOGLE_TALK;
        /** @since API level 1 */
        public static final int PROTOCOL_ICQ =
            android.provider.Contacts.ContactMethods.PROTOCOL_ICQ;
        /** @since API level 1 */
        public static final int PROTOCOL_JABBER =
            android.provider.Contacts.ContactMethods.PROTOCOL_JABBER;
        /** @since API level 1 */
        public static final int PROTOCOL_MSN =
            android.provider.Contacts.ContactMethods.PROTOCOL_MSN;
        /** @since API level 1 */
        public static final int PROTOCOL_QQ =
            android.provider.Contacts.ContactMethods.PROTOCOL_QQ;
        /** @since API level 1 */
        public static final int PROTOCOL_SKYPE =
            android.provider.Contacts.ContactMethods.PROTOCOL_SKYPE;
        /** @since API level 1 */
        public static final int PROTOCOL_YAHOO =
            android.provider.Contacts.ContactMethods.PROTOCOL_YAHOO;

        // constants inherited from the public interface
        // android.provider.BaseColumns

        /** @since API level 1 */
        public static final String _COUNT =
            android.provider.BaseColumns._COUNT;
        /** @since API level 1 */
        public static final String _ID =
            android.provider.BaseColumns._ID;

        // constants inherited from the public interface
        // android.provider.Contacts.ContactMethodsColumns

        /** @since API level 1 */
        public static final String AUX_DATA =
            android.provider.Contacts.ContactMethodsColumns.AUX_DATA;
        /** @since API level 1 */
        public static final String DATA =
            android.provider.Contacts.ContactMethodsColumns.DATA;
        /** @since API level 1 */
        public static final String ISPRIMARY =
            android.provider.Contacts.ContactMethodsColumns.ISPRIMARY;
        /** @since API level 1 */
        public static final String KIND =
            android.provider.Contacts.ContactMethodsColumns.KIND;
        /** @since API level 1 */
        public static final String LABEL =
            android.provider.Contacts.ContactMethodsColumns.LABEL;
        /** @since API level 1 */
        public static final String TYPE =
            android.provider.Contacts.ContactMethodsColumns.TYPE;
        /** @since API level 1 */
        public static final int TYPE_CUSTOM =
            android.provider.Contacts.ContactMethodsColumns.TYPE_CUSTOM;
        /** @since API level 1 */
        public static final int TYPE_HOME =
            android.provider.Contacts.ContactMethodsColumns.TYPE_HOME;
        /** @since API level 1 */
        public static final int TYPE_OTHER =
            android.provider.Contacts.ContactMethodsColumns.TYPE_OTHER;
        /** @since API level 1 */
        public static final int TYPE_WORK =
            android.provider.Contacts.ContactMethodsColumns.TYPE_WORK;

        // constants inherited from the public interface
        // android.provider.Contacts.PeopleColumns

        /** @since API level 1 */
        public static final String CUSTOM_RINGTONE =
            android.provider.Contacts.PeopleColumns.CUSTOM_RINGTONE;
        /** @since API level 1 */
        public static final String DISPLAY_NAME =
            android.provider.Contacts.PeopleColumns.DISPLAY_NAME;
        /** @since API level 1 */
        public static final String LAST_TIME_CONTACTED =
            android.provider.Contacts.PeopleColumns.LAST_TIME_CONTACTED;
        /** @since API level 1 */
        public static final String NAME =
            android.provider.Contacts.PeopleColumns.NAME;

    }

    // public static final class android.provider.Contacts.People
    @Deprecated
    public static final class People {

        /** @Since API level 1 */
        public static final android.net.Uri CONTENT_FILTER_URI =
            android.provider.Contacts.People.CONTENT_FILTER_URI;
        /** @Since API level 1 */
        public static final String CONTENT_ITEM_TYPE =
            android.provider.Contacts.People.CONTENT_ITEM_TYPE;
        /** @Since API level 1 */
        public static final String CONTENT_TYPE =
            android.provider.Contacts.People.CONTENT_TYPE;
        /** @Since API level 1 */
        public static final android.net.Uri CONTENT_URI =
            android.provider.Contacts.People.CONTENT_URI;
        /** @Since API level 1 */
        public static final String DEFAULT_SORT_ORDER =
            android.provider.Contacts.People.DEFAULT_SORT_ORDER;
        /** @Since API level 1 */
        public static final android.net.Uri DELETED_CONTENT_URI =
            android.provider.Contacts.People.DELETED_CONTENT_URI;
        /** @Since API level 1 */
/*
        // Constants found in Java documentation BUT not by javac !?

        public static final String NON_SYNCABLE_ACCOUNT =
            android.provider.Contacts.People.NON_SYNCABLE_ACCOUNT;
        /** @Since API level 1 * /
        public static final String NON_SYNCABLE_ACCOUNT_TYPE =
            android.provider.Contacts.People.NON_SYNCABLE_ACCOUNT_TYPE;
        /** @Since API level 1 * /
        public static final String PRIMARY_EMAIL_ID =
            android.provider.Contacts.People.PRIMARY_EMAIL_ID;
        /** @Since API level 1 * /
        public static final String PRIMARY_ORGANIZATION_ID =
            android.provider.Contacts.People.PRIMARY_ORGANIZATION_ID;
        /** @Since API level 1 * /
        public static final String PRIMARY_PHONE_ID =
            android.provider.Contacts.People.PRIMARY_PHONE_ID;
        /** @Since API level 1 * /
        public static final String _SYNC_ACCOUNT =
            android.provider.Contacts.People._SYNC_ACCOUNT;
        /** @Since API level 1 * /
        public static final String _SYNC_ACCOUNT_TYPE =
            android.provider.Contacts.People._SYNC_ACCOUNT_TYPE;
        /** @Since API level 1 * /
        public static final String _SYNC_DIRTY =
            android.provider.Contacts.People._SYNC_DIRTY;
        /** @Since API level 1 * /
        public static final String _SYNC_ID =
            android.provider.Contacts.People._SYNC_ID;
        /** @Since API level 1 * /
        public static final String _SYNC_LOCAL_ID =
            android.provider.Contacts.People._SYNC_LOCAL_ID;
        /** @Since API level 1 * /
        public static final String _SYNC_MARK =
            android.provider.Contacts.People._SYNC_MARK;
        /** @Since API level 1 * /
        public static final String _SYNC_TIME =
            android.provider.Contacts.People._SYNC_TIME;
        /** @Since API level 1 * /
        public static final String _SYNC_VERSION =
            android.provider.Contacts.People._SYNC_VERSION;
*/
        // constants inherited from the public interface
        // android.provider.BaseColumns

        /** @since API level 1 */
        public static final String _COUNT =
            android.provider.BaseColumns._COUNT;
        /** @since API level 1 */
        public static final String _ID =
            android.provider.BaseColumns._ID;

        // constants inherited from the public interface
        // android.provider.Contacts.PeopleColumns

        /** @since API level 1 */
        public static final String DISPLAY_NAME =
            android.provider.Contacts.PeopleColumns.DISPLAY_NAME;
        /** @since API level 1 */
        public static final String NAME =
            android.provider.Contacts.PeopleColumns.NAME;

    }

    // public static interface android.provider.Contacts.PeopleColumns
    @Deprecated
    public static final class PeopleColumns {

        /** @since API level 1 */
        public static final String CUSTOM_RINGTONE =
            android.provider.Contacts.PeopleColumns.CUSTOM_RINGTONE;
        /** @since API level 1 */
        public static final String DISPLAY_NAME =
            android.provider.Contacts.PeopleColumns.DISPLAY_NAME;
        /** @since API level 1 */
        public static final String LAST_TIME_CONTACTED =
            android.provider.Contacts.PeopleColumns.LAST_TIME_CONTACTED;
        /** @since API level 1 */
        public static final String NAME =
            android.provider.Contacts.PeopleColumns.NAME;
        /** @since API level 1 */
        public static final String NOTES =
            android.provider.Contacts.PeopleColumns.NOTES;
        /** @since API level 1 */
        public static final String PHONETIC_NAME =
            android.provider.Contacts.PeopleColumns.PHONETIC_NAME;
        /** @since API level 1 */
        public static final String PHOTO_VERSION =
            android.provider.Contacts.PeopleColumns.PHOTO_VERSION;
        /** @since API level 1 */
        public static final String SEND_TO_VOICEMAIL =
            android.provider.Contacts.PeopleColumns.SEND_TO_VOICEMAIL;
        /** @since API level 1 */
        public static final String STARRED =
            android.provider.Contacts.PeopleColumns.STARRED;
        /** @since API level 1 */
        public static final String TIMES_CONTACTED =
            android.provider.Contacts.PeopleColumns.TIMES_CONTACTED;

    }

    // public static final class android.provider.Contacts.Phones
    @Deprecated
    public static final class Phones {

        /** @Since API level 1 */
        public static final android.net.Uri CONTENT_FILTER_URL =
            android.provider.Contacts.Phones.CONTENT_FILTER_URL;
        /** @Since API level 1 */
        public static final String CONTENT_ITEM_TYPE =
            android.provider.Contacts.Phones.CONTENT_ITEM_TYPE;
        /** @Since API level 1 */
        public static final String CONTENT_TYPE =
            android.provider.Contacts.Phones.CONTENT_TYPE;
        /** @Since API level 1 */
        public static final android.net.Uri CONTENT_URI =
            android.provider.Contacts.Phones.CONTENT_URI;
        /** @Since API level 1 */
        public static final String DEFAULT_SORT_ORDER =
            android.provider.Contacts.Phones.DEFAULT_SORT_ORDER;
        /** @Since API level 1 */
        public static final String PERSON_ID =
            android.provider.Contacts.Phones.PERSON_ID;

        // constants inherited from the public interface
        // android.provider.BaseColumns

        /** @since API level 1 */
        public static final String _COUNT =
            android.provider.BaseColumns._COUNT;
        /** @since API level 1 */
        public static final String _ID =
            android.provider.BaseColumns._ID;

        // constants inherited from the public interface
        // android.provider.Contacts.PeopleColumns

        /** @since API level 1 */
        public static final String DISPLAY_NAME =
            android.provider.Contacts.PeopleColumns.DISPLAY_NAME;
        /** @since API level 1 */
        public static final String NAME =
            android.provider.Contacts.PeopleColumns.NAME;

        // constants inherited from the public interface
        // android.provider.Contacts.PhonesColumns

        /** @since API level 1 */
        public static final String ISPRIMARY =
            android.provider.Contacts.PhonesColumns.ISPRIMARY;
        /** @since API level 1 */
        public static final String LABEL =
            android.provider.Contacts.PhonesColumns.LABEL;
        /** @since API level 1 */
        public static final String NUMBER =
            android.provider.Contacts.PhonesColumns.NUMBER;
        /** @since API level 1 */
        public static final String NUMBER_KEY =
            android.provider.Contacts.PhonesColumns.NUMBER_KEY;

    }

}

