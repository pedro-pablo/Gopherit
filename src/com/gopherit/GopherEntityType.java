package com.gopherit;

public enum GopherEntityType
{
    TEXT_FILE('0'),
    MENU('1'),
    PHONEBOOK('2'),
    ERROR('3'),
    MAC_BINHEX('4'),
    DOS_BINARY('5'),
    UUENCODED('6'),
    SEARCH_SERVER('7'),
    TELNET('8'),
    BINARY('9'),
    MIRROR('+'),
    GIF('g'),
    IMAGE('I'),
    TN3270_TELNET('T'),
    HTML('h'),
    INFORMATION('i'),
    SOUND_FILE('s');

    private char associatedChar;

    GopherEntityType(char c) {
        this.associatedChar = c;
    }

    public char getAssociatedChar() {
        return associatedChar;
    }

    public static GopherEntityType getType(char associatedChar) {
        GopherEntityType[] types = GopherEntityType.values();
        for (GopherEntityType gopherEntityType : types) {
            if (gopherEntityType.getAssociatedChar() == associatedChar) {
                return gopherEntityType;
            }
        }
        return null;
    }

}