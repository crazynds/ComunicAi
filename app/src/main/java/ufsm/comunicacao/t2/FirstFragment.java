package ufsm.comunicacao.t2;

import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.navigation.fragment.NavHostFragment;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Consumer;

import ufsm.comunicacao.t2.databinding.FragmentFirstBinding;
import ufsm.comunicacao.t2.network.Comunication;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private Context context;
    private LinearLayout linearLayout;
    private ScrollView scrollView;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {


        binding = FragmentFirstBinding.inflate(inflater, container, false);
        context = getActivity().getApplicationContext();
        linearLayout = (LinearLayout)binding.linearlayout1;
        scrollView = (ScrollView)binding.scrollView1;
        EditText ipEditText = (EditText) binding.editTextIP;
        EditText portEditText = (EditText) binding.editTextPort;

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());

        try {
            Variable.address = InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e) {}

        ipEditText.setText(Variable.address.getHostAddress());
        portEditText.setText(""+Variable.port);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
        if(Variable.addressDestination!=null){
            Variable.connections.add(Variable.network.openComunication(Variable.addressDestination,Variable.portDestination));
            Variable.addressDestination = null;
        }
        for (Comunication c:
             Variable.connections) {
            addCard(c);
        }
        
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void addCard(Comunication c){

        CardView card = new CardView(context);

        // Set the CardView layoutParams
        MarginLayoutParams params = new MarginLayoutParams(
                MarginLayoutParams.MATCH_PARENT,
                MarginLayoutParams.WRAP_CONTENT
        );
        params.setMargins(0,10,0,10);
        card.setLayoutParams(params);


        // Set CardView corner radius
        card.setRadius(9);

        // Set cardView content padding
        card.setContentPadding(15, 15, 15, 15);

        // Set a background color for CardView
        card.setCardBackgroundColor(Color.parseColor("#FFC6D6C3"));

        // Set the CardView maximum elevation
        card.setMaxCardElevation(15);

        // Set CardView elevation
        card.setCardElevation(9);

        // Initialize a new TextView to put in CardView
        TextView tv = new TextView(context);
        tv.setLayoutParams(params);
        tv.setText("Ip: "+c.getAddress()+":"+c.getPort()+"\nOFFLINE");
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
        tv.setTextColor(Color.RED);

        c.sendPing(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                tv.setText("Ip: "+c.getAddress()+":"+c.getPort()+"\nONLINE: "+integer+" ms");
            }
        });
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Variable.currentComunication = c;
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_ThirdFragment);
            }
        });

        // Put the TextView in CardView
        card.addView(tv);

        linearLayout.addView(card);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}