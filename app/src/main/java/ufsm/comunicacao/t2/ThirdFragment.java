package ufsm.comunicacao.t2;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import ufsm.comunicacao.t2.databinding.FragmentThirdBinding;
import ufsm.comunicacao.t2.network.Comunication;
import ufsm.comunicacao.t2.network.ComunicationLinstener;

public class ThirdFragment extends Fragment {
    private FragmentThirdBinding binding;

    private Comunication currentComunication;

    private Context context;
    private TextView ipText,portText,sucessoText,erroText,repetidosText,tempoRespostaText;
    private LinearLayout linearLayout;

    private int tempoResposta=0;
    private boolean ativo = false;

    private boolean sendRandomText = false;

    private Handler handler;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentThirdBinding.inflate(inflater, container, false);
        context = getActivity().getApplicationContext();

        this.ativo = true;
        this.currentComunication = Variable.currentComunication;


        ipText = (TextView) binding.textViewIp;
        portText = (TextView) binding.textViewPorta;
        sucessoText = (TextView) binding.textViewSucesso;
        erroText = (TextView) binding.textViewErro;
        repetidosText = (TextView) binding.textViewFramesRepetitidos;
        tempoRespostaText = (TextView) binding.textViewTempoDeResposta;
        linearLayout = (LinearLayout)binding.mensagensLayout;

        ((Button)binding.pingButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviaPing();
            }
        });
        ((Button)binding.backButton2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ativo=false;
                NavHostFragment.findNavController(ThirdFragment.this)
                        .navigate(R.id.action_ThirdFragment_to_FirstFragment);
            }
        });
        ((Button)binding.flowButton).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        sendRandomText = true;
                        return true;
                    case MotionEvent.ACTION_UP:
                        sendRandomText = false;
                        return true;
                }
                return false;
            }
        });
        ((Button)binding.enviaMensagem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = ((EditText)binding.editTextMensagem).getText().toString();
                ((EditText)binding.editTextMensagem).setText("");
                if(!msg.isEmpty()) {
                    addCard(msg, false);
                }
            }
        });
        ((Button)binding.clearButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Variable.network.remove(currentComunication);
                Variable.connections.remove(currentComunication);
                currentComunication.end();
                Variable.currentComunication = null;
                ativo=false;
                NavHostFragment.findNavController(ThirdFragment.this)
                        .navigate(R.id.action_ThirdFragment_to_FirstFragment);
            }
        });
        ((Button)binding.resyncButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Variable.network.remove(currentComunication);
                Variable.connections.remove(currentComunication);
                currentComunication.end();
                currentComunication = Variable.network.openComunication(currentComunication.getAddress(),currentComunication.getPort());
                Variable.connections.add(currentComunication);
                Variable.currentComunication = currentComunication;

                Variable.errorFrames = 0;
                Variable.sucessFrames = 0;
                Variable.repetidosFrames = 0;
            }
        });

        ipText.setText("IP: "+currentComunication.getAddress().getHostAddress());
        portText.setText("Port: "+currentComunication.getPort());

        Variable.errorFrames = 0;
        Variable.sucessFrames = 0;
        Variable.repetidosFrames = 0;
        tempoResposta= 0 ;
        handler = new Handler();

        rodaThread();
        enviaPing();
        retext();

        return binding.getRoot();
    }

    private List<TextView> textConfirmed = Collections.synchronizedList(new ArrayList<>());

    private void addCard(String text,boolean recived){

        CardView card = new CardView(context);

        // Set the CardView layoutParams
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.MarginLayoutParams.MATCH_PARENT,
                ViewGroup.MarginLayoutParams.WRAP_CONTENT
        );
        params.setMargins(recived?200:30,10,recived?30:200,10);
        card.setLayoutParams(params);
        card.setRadius(4);
        card.setContentPadding(15, 15, 15, 15);
        card.setCardBackgroundColor(Color.parseColor("#91f690"));
        card.setMaxCardElevation(8);
        card.setCardElevation(6);

        ViewGroup.MarginLayoutParams params2 = new ViewGroup.MarginLayoutParams(
                ViewGroup.MarginLayoutParams.MATCH_PARENT,
                ViewGroup.MarginLayoutParams.WRAP_CONTENT
        );
        params2.setMargins(0,5,0,5);
        TextView tv = new TextView(context);
        tv.setLayoutParams(params2);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        tv.setTextColor(Color.BLACK);

        // Put the TextView in CardView
        card.addView(tv);
        if(!recived)
            currentComunication.sendText(text, new Runnable() {
                @Override
                public void run() {
                    Log.i("NETWORK-ANK","Confirmou o pacote: "+text);
                    textConfirmed.add(tv);
                }
            });

        linearLayout.addView(card,0);
    }


    private void rodaThread(){
        if(!ativo)return;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                retext();
                rodaThread();
            }
        },200);
    }

    private boolean waitingPing = false;

    private void enviaPing(){
        tempoResposta=0;
        if(waitingPing)return;
        waitingPing = true;
        currentComunication.sendPing(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                Log.i("NETWORK","RECEBI PING "+integer);
                tempoResposta = integer;
                retext();
                waitingPing = false;
            }
        });
    }

    private void retext(){
        if(tempoResposta<=0)
            tempoRespostaText.setText("Tempo de Resposta: SEM RESPOSTA");
        else
            tempoRespostaText.setText("Tempo de Resposta: "+tempoResposta+"ms");
        erroText.setText("Frames com Erro: "+Variable.errorFrames);
        repetidosText.setText("Frames repetidos: "+Variable.repetidosFrames);
        sucessoText.setText("Frames com sucesso: "+Variable.sucessFrames);

        String line;
        while((line = currentComunication.nextLine())!=null){
            if(!line.isEmpty())
                addCard(line,true);
        }
        try{
            do {
                TextView tv = textConfirmed.remove(0);
                tv.setText(tv.getText()+"\n ✔️");
            }while(true);
        }catch (IndexOutOfBoundsException e){

        }


        if(sendRandomText){
            line = randomSentence();
            addCard(line, false);
        }
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private String randomSentence(){
        String article[] = { "the", "a", "one", "some", "any" };
        // define noun, verb, preposition in same way here
        String noun[]={"boy","men","girl","house","door","dog","car","bicycle"};
        String verb[]={"ran","jumped","sang","moves","reads","acts","eats","does"};
        String preposition[]={"away","towards","around","near","far away","somewhere"};

        Random generator = new Random();
        int article1 = generator.nextInt( article.length );
        // generat others here like noun1, verb1, ....
        int noun1 = generator.nextInt( article.length );
        int verb1 = generator.nextInt( article.length );
        int preposition1 =generator.nextInt(article.length);
        int article2=generator.nextInt(article.length);
        int noun2=generator.nextInt(article.length);

        StringBuilder buffer = new StringBuilder();

        // concatenate words and add period
        buffer.append(article[article1]+" ").append(noun[noun1]+" ").append
                (verb[verb1]+" ").append(preposition[preposition1]+" ").append
                (article[article2]+" ").append(noun[noun2]).append(".");

        // capitalize first letter and display
        buffer.setCharAt(
                0, Character.toUpperCase( buffer.charAt( 0 ) ) );
        return buffer.toString();
    }

}
