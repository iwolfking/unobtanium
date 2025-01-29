package xyz.iwolfking.unobtainium.api.compound;


import java.util.ArrayList;

import iskallia.vault.core.card.Card;
import iskallia.vault.core.data.DataList;


/**
 * This is simple class that allows to serialize and deserialize list of cards using VH data list handling.
 */
public class CardList extends DataList<CardList, Card>
{
    public CardList()
    {
        super(new ArrayList<>(), Card.ADAPTER);
    }
}