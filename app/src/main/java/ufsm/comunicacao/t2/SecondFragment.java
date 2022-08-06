package ufsm.comunicacao.t2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.net.InetAddress;

import ufsm.comunicacao.t2.databinding.FragmentSecondBinding;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;


    private EditText ipDestination,portDestination;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);

        ipDestination = (EditText) binding.editTextIpDestination;
        portDestination = (EditText) binding.editTextPortDestination;
        return binding.getRoot();

    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
        binding.confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String portStr = portDestination.getText().toString();
                String ipStr = ipDestination.getText().toString();

                InetAddress address;
                int port = 0;

                if(!portStr.matches("-?\\d+")){
                    return;//Error por nao ser um inteiro
                }
                port = Integer.valueOf(portStr);

                if(port<100 && port> 65000)return;

                try{
                    address=InetAddress.getByName(ipStr.trim());
                }catch(Exception e){
                    return;//Error por não ser um ip válido
                }

                Variable.addressDestination = address;
                Variable.portDestination = port;

                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}