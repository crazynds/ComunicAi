package ufsm.comunicacao.t2;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import ufsm.comunicacao.t2.databinding.ActivityMainBinding;
import ufsm.comunicacao.t2.network.Comunication;
import ufsm.comunicacao.t2.network.NetworkListener;
import ufsm.comunicacao.t2.network.NetworkManager;

import android.view.Menu;
import android.view.MenuItem;

import java.net.SocketException;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        if(Variable.network==null){
            try {
                Variable.network = new NetworkManager(new NetworkListener() {
                    @Override
                    public void onOpenConnection(Comunication c) {
                        Variable.connections.add(c);
                    }
                }, Variable.port);
            } catch (SocketException e) {
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

}