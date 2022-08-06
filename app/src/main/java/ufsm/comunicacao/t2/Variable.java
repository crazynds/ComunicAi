package ufsm.comunicacao.t2;

import androidx.cardview.widget.CardView;

import java.net.InetAddress;
import java.util.ArrayList;

import ufsm.comunicacao.t2.network.Comunication;
import ufsm.comunicacao.t2.network.NetworkManager;

public final class Variable {


    public static final int port = 42971;
    public static InetAddress address;

    public static int portDestination;
    public static InetAddress addressDestination;

    public static ArrayList<Comunication> connections = new ArrayList<>();

    public static NetworkManager network = null;

    public static Comunication currentComunication = null;

    public static int errorFrames = 0,repetidosFrames = 0, sucessFrames = 0;



}
