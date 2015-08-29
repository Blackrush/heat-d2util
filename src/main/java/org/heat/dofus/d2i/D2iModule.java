package org.heat.dofus.d2i;

import java.util.stream.LongStream;
import java.util.stream.Stream;

public interface D2iModule {
    /**
     * Return the lang of this module.
     */
    String getLang();

    /**
     * Return a localized message by its id.
     * @param id the message's id
     * @return a non-null message
     * @throws java.util.NoSuchElementException if the id hasnt any message
     */
    String byId(long id);

    /**
     * Return a localized message by its name.
     * @param name the message's name
     * @return a non-null message
     * @throws java.util.NoSuchElementException if the name hasnt any message
     */
    String byName(String name);

    /**
     * Return a diacritical localized message by its id.
     * @param id the message's id
     * @return a non-null message
     * @throws java.util.NoSuchElementException if the id hasnt any message
     */
    String diacritical(long id);

    /**
     * Return all the possible IDs of this module
     */
    LongStream keys();

    /**
     * Return all the possible names of this module
     */
    Stream<String> namedKeys();

    /**
     * Determine whether or not a message is diacritical.
     * @param id the message's id
     */
    boolean isDiacritical(long id);

    /**
     * Determine whether or not this module contains the message ID
     * @param id the message's id to test
     */
    boolean hasMessageId(long id);

    /**
     * Determine whether or not this module contians the message name
     * @param name the message's name  to test
     */
    boolean hasMessageName(String name);
}
