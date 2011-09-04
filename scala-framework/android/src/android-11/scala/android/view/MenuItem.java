/*                     __                                               *\
**     ________ ___   / /  ___     Scala Android                        **
**    / __/ __// _ | / /  / _ |    (c) 2009-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */


package scala.android.view;

/*
 * This helper class is a workaround for accessing Java static constants from
 * Scala code when whose constants are defined in Java static inner interfaces.
 *
 * @author Stephane Micheloud
 * @version 1.0
 */
//public final class android.view.MenuItem
public final class MenuItem {

    /** @since API level 11 */
    public static final int SHOW_AS_ACTION_ALWAYS =
        android.view.MenuItem.SHOW_AS_ACTION_ALWAYS;

    /** @since API level 11 */
    public static final int SHOW_AS_ACTION_IF_ROOM =
        android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;

    /** @since API level 11 */
    public static final int SHOW_AS_ACTION_NEVER =
        android.view.MenuItem.SHOW_AS_ACTION_NEVER;

    /** @since API level 11 */
    public static final int SHOW_AS_ACTION_WITH_TEXT =
        android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT;
}
        