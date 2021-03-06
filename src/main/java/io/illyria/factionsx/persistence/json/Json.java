package io.illyria.factionsx.persistence.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.illyria.factionsx.entity.FPlayer;
import io.illyria.factionsx.entity.Faction;
import io.illyria.factionsx.entity.IFPlayer;
import io.illyria.factionsx.entity.IFaction;
import io.illyria.factionsx.internal.InterfaceSerializer;
import io.illyria.factionsx.persistence.Dispatcher;

public final class Json extends Dispatcher {

    private Gson gson;

    public Json() {
        gson = new GsonBuilder().setPrettyPrinting().serializeNulls()
                .registerTypeAdapter(IFaction.class, InterfaceSerializer.interfaceSerializer(Faction.class))
                .registerTypeAdapter(IFPlayer.class, InterfaceSerializer.interfaceSerializer(FPlayer.class))
                .create();

        this.fPlayerPersistece = new JsonPlayer();
        this.factionPersistence = new JsonFaction();
    }

    public Gson getGson() {
        return gson;
    }
}
