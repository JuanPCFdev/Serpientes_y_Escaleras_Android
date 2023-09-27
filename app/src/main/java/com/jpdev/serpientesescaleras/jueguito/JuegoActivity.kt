package com.jpdev.serpientesescaleras.jueguito

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.jpdev.serpientesescaleras.R
import java.util.Locale
import kotlin.random.Random

class JuegoActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    @SuppressLint("MissingInflatedId")

    private var tts:TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego)

        val Jugador1: MutableList<Jugada> = ArrayList() //Declaramos lista de jugadas para jugador 1
        val Jugador2: MutableList<Jugada> = ArrayList() //Declaramos lista de jugadas para jugador 2
        val Botones: MutableList<Button> = crearListaBotones() //Declaramos lista de botones que contiene el tablero
        Botones[0].setBackgroundColor(Color.MAGENTA) //Inicializamos el boton de inicio, con el color de 2 jugadores

        val btnJugar = findViewById<Button>(R.id.btnJugar) //Declaramos el boton de "JUGAR"
        val dado1 = findViewById<TextView>(R.id.dado1) //Declaramos el TextView que representa el primer Dado
        val dado2 = findViewById<TextView>(R.id.dado2) //Declaramos el TextView que representa el segundo Dado

        var ContTurno:Int = 0 //Declaramos una variable para saber si el turno es del jugador uno o dos
        var contGame = 0 //Declaramos una variable que ayuda a saber si se han realizado jugadas o aun no se han hecho jugadas


        tts = TextToSpeech(this,this)



        btnJugar.setOnClickListener { //Inicializamos un Onclick Listener para el boton "JUGAR"
            val jugada:Jugada = play() //Esta funcion retorna un objeto con los dos numeros de los dados de la jugada

            dado1.setText("${jugada.Dado1}") //se asigna el valor del dado 1 de la jugada al TextView que representa dicho dado
            dado2.setText("${jugada.Dado2}") //se asigna el valor del dado 2 de la jugada al TextView que representa dicho dado
            if(ContTurno == 0){//El ContTurno nos ayuda a saber si el turno es del jugador 1 o 2 (Jugador1 = 0 / Jugador2 = 1)
                Jugador1.add(jugada) //Añadimos el objeto de la jugada actual a la lista del jugador1
                borrarRastroJugador1(Jugador1,Botones) //Borramos las posiciones en el tablero que ha ocupado el jugador 1

                val recorrido1:Int = SumarCasillas(Jugador1) //Con la funcion SumarCasillas se puede saber la posicion actual del jugador
                val recorrido2:Int = SumarCasillas(Jugador2)

                Speak("Player 1 move to position $recorrido1")

                if(recorrido1 == recorrido2){//Este condicional verifica si las dos fichas estan o no en la misma casilla
                    Botones[recorrido1-1].setBackgroundColor(Color.MAGENTA)

                }else{
                    borrarRastroMagentaJugador1(Jugador1,Botones)
                    Botones[recorrido1-1].setBackgroundColor(Color.BLUE)
                    //Los botones estan en un orden de 0 a 23 (24Botones) por eso es necesario restarle 1 ya que el boton 1 es la posicion 0
                }

                if(recorrido1==24){//Si el jugador llega a la casilla final GANA
                    val intent = Intent(this,GanadorActivity::class.java)
                    Ganador("JUGADOR 1",intent)
                }

                descenso(Jugador1,recorrido1,Botones)
                ascenso(Jugador1,recorrido1,Botones)

                if(contGame==0){
                    Botones[0].setBackgroundColor(Color.RED)//Cuando parte de inicio, deja atras al jugador 2 en inicio
                    contGame++//Se incrementa el contador para que nunca vuelva a entrar a este ciclo
                }

                if(jugada.Dado1 + jugada.Dado2 == 6){ //Condicional para repetir turno
                    ContTurno = 0 //Si saca 6 repite turno
                    backToStart(Jugador1,Botones)
                }else{
                    ContTurno = 1 //Cambia de turno a jugador 2
                }

            }else if(ContTurno == 1){//Turno de jugador 2
                //Pasa lo mismo que con el jugador 1
                Jugador2.add(jugada)
                borrarRastroJugador2(Jugador2,Botones)

                val recorrido1:Int = SumarCasillas(Jugador1)
                val recorrido2:Int = SumarCasillas(Jugador2)//Recorrido

                Speak("Player 2 move to position $recorrido2")

                if(recorrido1 == recorrido2){
                    Botones[recorrido2-1].setBackgroundColor(Color.MAGENTA)

                }else{
                    borrarRastroMagentaJugador2(Jugador2,Botones)
                    Botones[recorrido2-1].setBackgroundColor(Color.RED)
                }

                descenso2(Jugador2,recorrido2,Botones)
                ascenso2(Jugador2,recorrido2,Botones)

                if(recorrido2==24){//Si el jugador llega a la casilla final GANA
                    val intent = Intent(this,GanadorActivity::class.java)
                    Ganador("JUGADOR 2",intent)
                }

                Botones[0].setBackgroundColor(Color.TRANSPARENT)//Cuando parte del inicio, se vuelve transparente el boton de inicio

                if(jugada.Dado1 + jugada.Dado2 == 6){ //Condicional para repetir turno
                    ContTurno = 1 //Si saca 6 repite turno
                    backToStart(Jugador2,Botones)
                }else{
                    ContTurno = 0 //Cambia de turno a jugador 1
                }
            }

        }

    }

    override fun onDestroy() {
        if(tts!=null){
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    fun Speak(texto:String){
        tts!!.speak(texto,TextToSpeech.QUEUE_FLUSH,null,"")
    }

    override fun onInit(p0: Int) {
        if(p0 == TextToSpeech.SUCCESS){
            var output = tts!!.setLanguage(Locale.US)

            if(output == TextToSpeech.LANG_MISSING_DATA || output == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TTS","El lenguaje no es permitido")
            }
        }else{
            Log.e("TTS","Inicializacion Fallida")
        }
    }

    fun backToStart(Jugador: MutableList<Jugada>, Botones: MutableList<Button>){
        var contadorDeSeis = 0
        var recorrido = 0

        var botonR = Button(this)
        botonR.setBackgroundColor(Color.RED)
        val botonRojo = (botonR.background as? ColorDrawable)?.color

        var botonA = Button(this)
        botonA.setBackgroundColor(Color.BLUE)
        val botonAzul = (botonA.background as? ColorDrawable)?.color

        Jugador.forEach {
            if(it.Dado1 + it.Dado2 == 6){
                contadorDeSeis ++

                if(contadorDeSeis == 3){

                    Jugador.forEach {
                        recorrido += it.Suma
                    }

                    Jugador.clear()

                    val botonDeRecorrido = (Botones[recorrido-1].background as? ColorDrawable)?.color
                    val botonDeInicio = (Botones[0].background as? ColorDrawable)?.color

                    if(botonDeRecorrido == botonRojo){
                        Botones[recorrido-1].setBackgroundColor(Color.TRANSPARENT)

                        if(botonDeInicio == botonAzul){
                            Botones[0].setBackgroundColor(Color.MAGENTA)
                        }else{
                            Botones[0].setBackgroundColor(Color.RED)
                        }
                    }else if(botonDeRecorrido == botonAzul){
                        Botones[recorrido-1].setBackgroundColor(Color.TRANSPARENT)

                        if(botonDeInicio == botonRojo){
                            Botones[0].setBackgroundColor(Color.MAGENTA)
                        }else{
                            Botones[0].setBackgroundColor(Color.BLUE)
                        }
                    }



                    contadorDeSeis = 0

                }
            }else{
                contadorDeSeis = 0
            }
        }

    }

    fun ascenso(Jugador: MutableList<Jugada>,recorrido:Int, Botones: MutableList<Button>){
        var boton = Button(this)
        boton.setBackgroundColor(Color.RED)
        val botonRojo = (boton.background as? ColorDrawable)?.color

        when (recorrido){
            3 -> {
                Jugador.forEach {
                    it.Suma = 0
                }
                Jugador[0].Suma = 11
                Botones[2].setBackgroundColor(Color.TRANSPARENT)
                val botonSubida = (Botones[10].background as? ColorDrawable)?.color

                if(botonSubida == botonRojo){
                    Botones[10].setBackgroundColor(Color.MAGENTA)
                }else{
                    Botones[10].setBackgroundColor(Color.BLUE)
                }
            }
            10 -> {
                Jugador.forEach {
                    it.Suma = 0
                }
                Jugador[0].Suma = 15
                Botones[9].setBackgroundColor(Color.TRANSPARENT)
                val botonSubida = (Botones[14].background as? ColorDrawable)?.color

                if(botonSubida == botonRojo){
                    Botones[14].setBackgroundColor(Color.MAGENTA)
                }else{
                    Botones[14].setBackgroundColor(Color.BLUE)
                }
            }
            16 -> {
                Jugador.forEach {
                    it.Suma = 0
                }
                Jugador[0].Suma = 17
                Botones[15].setBackgroundColor(Color.TRANSPARENT)

                val botonSubida = (Botones[16].background as? ColorDrawable)?.color

                if(botonSubida == botonRojo){
                    Botones[16].setBackgroundColor(Color.MAGENTA)
                }else{
                    Botones[16].setBackgroundColor(Color.BLUE)
                }
            }
            else -> {
                //Todo ok, no pasa nada capo
            }
        }
    }

    fun ascenso2(Jugador: MutableList<Jugada>,recorrido:Int, Botones: MutableList<Button>){
        var boton = Button(this)
        boton.setBackgroundColor(Color.BLUE)
        val botonAzul = (boton.background as? ColorDrawable)?.color

        when (recorrido){
            3 -> {
                Jugador.forEach {
                    it.Suma = 0
                }
                Jugador[0].Suma = 11
                Botones[2].setBackgroundColor(Color.TRANSPARENT)
                val botonSubida = (Botones[10].background as? ColorDrawable)?.color

                if(botonSubida == botonAzul){
                    Botones[10].setBackgroundColor(Color.MAGENTA)
                }else{
                    Botones[10].setBackgroundColor(Color.RED)
                }
            }
            10 -> {
                Jugador.forEach {
                    it.Suma = 0
                }
                Jugador[0].Suma = 15
                Botones[9].setBackgroundColor(Color.TRANSPARENT)
                val botonSubida = (Botones[14].background as? ColorDrawable)?.color

                if(botonSubida == botonAzul){
                    Botones[14].setBackgroundColor(Color.MAGENTA)
                }else{
                    Botones[14].setBackgroundColor(Color.RED)
                }
            }
            16 -> {
                Jugador.forEach {
                    it.Suma = 0
                }
                Jugador[0].Suma = 17
                Botones[15].setBackgroundColor(Color.TRANSPARENT)

                val botonSubida = (Botones[16].background as? ColorDrawable)?.color

                if(botonSubida == botonAzul){
                    Botones[16].setBackgroundColor(Color.MAGENTA)
                }else{
                    Botones[16].setBackgroundColor(Color.RED)
                }
            }
            else -> {
                //Todo ok, no pasa nada capo
            }
        }
    }

    fun descenso(Jugador: MutableList<Jugada>,recorrido:Int, Botones: MutableList<Button>){
        var boton = Button(this)
        boton.setBackgroundColor(Color.RED)
        val botonRojo = (boton.background as? ColorDrawable)?.color

            when (recorrido){
                7 -> {
                    Jugador.forEach {
                        it.Suma = 0
                    }
                    Jugador[0].Suma = 2
                    Botones[6].setBackgroundColor(Color.TRANSPARENT)
                    val botonCaida = (Botones[1].background as? ColorDrawable)?.color

                    if(botonCaida == botonRojo){
                        Botones[1].setBackgroundColor(Color.MAGENTA)
                    }else{
                        Botones[1].setBackgroundColor(Color.BLUE)
                    }
                }
                20 -> {
                    Jugador.forEach {
                        it.Suma = 0
                    }
                    Jugador[0].Suma = 12
                    Botones[19].setBackgroundColor(Color.TRANSPARENT)
                    val botonCaida = (Botones[11].background as? ColorDrawable)?.color

                    if(botonCaida == botonRojo){
                        Botones[11].setBackgroundColor(Color.MAGENTA)
                    }else{
                        Botones[11].setBackgroundColor(Color.BLUE)
                    }
                }
                23 -> {
                    Jugador.forEach {
                        it.Suma = 0
                    }
                    Jugador[0].Suma = 18
                    Botones[22].setBackgroundColor(Color.TRANSPARENT)

                    val botonCaida = (Botones[17].background as? ColorDrawable)?.color

                    if(botonCaida == botonRojo){
                        Botones[17].setBackgroundColor(Color.MAGENTA)
                    }else{
                        Botones[17].setBackgroundColor(Color.BLUE)
                    }
                }
                else -> {
                    //Todo ok, no pasa nada capo
                }
            }
    }

    fun descenso2(Jugador: MutableList<Jugada>,recorrido:Int, Botones: MutableList<Button>){
        var boton = Button(this)
        boton.setBackgroundColor(Color.BLUE)
        val botonAzul = (boton.background as? ColorDrawable)?.color

        when (recorrido){
            7 -> {
                Jugador.forEach {
                    it.Suma = 0
                }
                Jugador[0].Suma = 2
                Botones[6].setBackgroundColor(Color.TRANSPARENT)
                val botonCaida = (Botones[1].background as? ColorDrawable)?.color

                if(botonCaida == botonAzul){
                    Botones[1].setBackgroundColor(Color.MAGENTA)
                }else{
                    Botones[1].setBackgroundColor(Color.RED)
                }
            }
            20 -> {
                Jugador.forEach {
                    it.Suma = 0
                }
                Jugador[0].Suma = 12
                Botones[19].setBackgroundColor(Color.TRANSPARENT)
                val botonCaida = (Botones[11].background as? ColorDrawable)?.color

                if(botonCaida == botonAzul){
                    Botones[11].setBackgroundColor(Color.MAGENTA)
                }else{
                    Botones[11].setBackgroundColor(Color.RED)
                }
            }
            23 -> {
                Jugador.forEach {
                    it.Suma = 0
                }
                Jugador[0].Suma = 18
                Botones[22].setBackgroundColor(Color.TRANSPARENT)
                val botonCaida = (Botones[17].background as? ColorDrawable)?.color

                if(botonCaida == botonAzul){
                    Botones[17].setBackgroundColor(Color.MAGENTA)
                }else{
                    Botones[17].setBackgroundColor(Color.RED)
                }
            }
            else -> {
                //Todo ok, no pasa nada capo
            }
        }
    }

    fun Ganador(ganador:String,intent:Intent){
        intent.putExtra("ganador",ganador)
        startActivity(intent)
    }

    fun borrarRastroMagentaJugador1(Jugador: MutableList<Jugada>, Botones: MutableList<Button>){
        var sumadorCasillas = 0
        var boton = Button(this)
        boton.setBackgroundColor(Color.MAGENTA)
        Jugador.forEach{
            sumadorCasillas += it.Suma
            if(sumadorCasillas>=24){
                sumadorCasillas = 24
            }
            val botonBackground = (boton.background as? ColorDrawable)?.color
            val BotonesBackground = (Botones[sumadorCasillas-1].background as? ColorDrawable)?.color

            if(botonBackground == BotonesBackground){
                Botones[sumadorCasillas-1].setBackgroundColor(Color.RED)
            }
        }
    }

    fun borrarRastroMagentaJugador2(Jugador: MutableList<Jugada>, Botones: MutableList<Button>){
        var sumadorCasillas = 0
        var boton = Button(this)
        boton.setBackgroundColor(Color.MAGENTA)
        Jugador.forEach{
            sumadorCasillas += it.Suma
            if(sumadorCasillas>=24){
                sumadorCasillas = 24
            }
            val botonBackground = (boton.background as? ColorDrawable)?.color
            val BotonesBackground = (Botones[sumadorCasillas-1].background as? ColorDrawable)?.color

            if(botonBackground == BotonesBackground){
                Botones[sumadorCasillas-1].setBackgroundColor(Color.BLUE)
            }
        }
    }

    fun borrarRastroJugador1(Jugador: MutableList<Jugada>, Botones: MutableList<Button>){ //Esta funcion ayuda a borrar las posicones en las que estubo el jugador 1
        var sumadorCasillas = 0 //Esta variable ayuda a saber la posicion final del jugador, ya que suma el total de numero de dados de las jugadas
        var boton = Button(this) //Creamos un boton auxiliar para comparar los colores de los botones del tablero
        boton.setBackgroundColor(Color.BLUE) //Le asignamos el color azul que corresponde al jugador 1
        Jugador.forEach{ //Con el forEach recorremos toda la lista de las jugadas del jugador1
            sumadorCasillas += it.Suma //El atributo suma contiene la suma de los dos dados que ha tenido el jugador en cada jugada
            if(sumadorCasillas>=24){ //El tablero solo tiene 24 casillas, pero los dados y las jugadas pueden sumar mas
                sumadorCasillas = 24 //Por eso se obliga al sumador de casillas a bloquearse en el numero 24
            }
            val botonBackground = (boton.background as? ColorDrawable)?.color //Extraemos el valor del color del boton auxiliar
            val BotonesBackground = (Botones[sumadorCasillas-1].background as? ColorDrawable)?.color //Extraemos el valor del color del boton en el cual ha pasado el jugador

            if(botonBackground == BotonesBackground){//Si alguna casilla por la cual ha pasado el jugador esta de color Azul entra al ciclo
                Botones[sumadorCasillas-1].setBackgroundColor(Color.TRANSPARENT)//Se borra el color azul de boton y se vuelve transparente
            }
        }
    }
//Lo mismo que el borrarRastroJugador1 pero con el color Rojo para el jugador 2
    fun borrarRastroJugador2(Jugador: MutableList<Jugada>, Botones: MutableList<Button>){
        var sumadorCasillas = 0
        var boton = Button(this)
        boton.setBackgroundColor(Color.RED)
        Jugador.forEach{
            sumadorCasillas += it.Suma
            if(sumadorCasillas>=24){
                sumadorCasillas = 24
            }
            val botonBackground = (boton.background as? ColorDrawable)?.color
            val BotonesBackground = (Botones[sumadorCasillas-1].background as? ColorDrawable)?.color

            if(botonBackground == BotonesBackground){
                Botones[sumadorCasillas-1].setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    fun SumarCasillas(Jugador:MutableList<Jugada>):Int{//Esta funcion suma el total de los dados en cada jugada y asi se puede saber la posicion actual del jugador
        var sumadorCasillas = 0 //Variable que contendra el valor total que ha recorrido el jugador
        Jugador.forEach{
                sumadorCasillas += it.Suma //Se extrae el valor de Suma de todos los objetos de la lisa y se van sumando
        }
        if(sumadorCasillas>=24){
            sumadorCasillas = 24 //Se limita el valor a 24 ya que si fuera mas, se romperia el codigo y lloraria Android
         }
        return sumadorCasillas //Se retorna el recorrido total del jugador
    }

    fun play():Jugada{ //La funcion Play ayuda a crear y retornar un objeto de tipo Jugada el cual tendra dos numeros de dados aleatorios
        val dado1:Int = Random.nextInt(1,4) //Se usa un random para representar el numero del dado 1
        val dado2:Int = Random.nextInt(1,4) //Se usa un random para representar el numero del dado 2
        val jugada = Jugada(dado1,dado2) //Se usa el constructor para asignar el dado 1 y 2
        return jugada //Se retorna el objeto de tipo jugada
    }

    //Inicializamos todos los botones en la lista de botones del tablero
    fun crearListaBotones(): MutableList<Button> {
        val listaMutable = mutableListOf<Button>()
        val btn1 = findViewById<Button>(R.id.btn1)
        val btn2 = findViewById<Button>(R.id.btn2)
        val btn3 = findViewById<Button>(R.id.btn3)
        val btn4 = findViewById<Button>(R.id.btn4)
        val btn5 = findViewById<Button>(R.id.btn5)
        val btn6 = findViewById<Button>(R.id.btn6)
        val btn7 = findViewById<Button>(R.id.btn7)
        val btn8 = findViewById<Button>(R.id.btn8)
        val btn9 = findViewById<Button>(R.id.btn9)
        val btn10 = findViewById<Button>(R.id.btn10)
        val btn11 = findViewById<Button>(R.id.btn11)
        val btn12 = findViewById<Button>(R.id.btn12)
        val btn13 = findViewById<Button>(R.id.btn13)
        val btn14 = findViewById<Button>(R.id.btn14)
        val btn15 = findViewById<Button>(R.id.btn15)
        val btn16 = findViewById<Button>(R.id.btn16)
        val btn17 = findViewById<Button>(R.id.btn17)
        val btn18 = findViewById<Button>(R.id.btn18)
        val btn19 = findViewById<Button>(R.id.btn19)
        val btn20 = findViewById<Button>(R.id.btn20)
        val btn21 = findViewById<Button>(R.id.btn21)
        val btn22 = findViewById<Button>(R.id.btn22)
        val btn23 = findViewById<Button>(R.id.btn23)
        val btn24 = findViewById<Button>(R.id.btn24)

        listaMutable.add(btn1)
        listaMutable.add(btn2)
        listaMutable.add(btn3)
        listaMutable.add(btn4)
        listaMutable.add(btn5)
        listaMutable.add(btn6)
        listaMutable.add(btn7)
        listaMutable.add(btn8)
        listaMutable.add(btn9)
        listaMutable.add(btn10)
        listaMutable.add(btn11)
        listaMutable.add(btn12)
        listaMutable.add(btn13)
        listaMutable.add(btn14)
        listaMutable.add(btn15)
        listaMutable.add(btn16)
        listaMutable.add(btn17)
        listaMutable.add(btn18)
        listaMutable.add(btn19)
        listaMutable.add(btn20)
        listaMutable.add(btn21)
        listaMutable.add(btn22)
        listaMutable.add(btn23)
        listaMutable.add(btn24)

        return listaMutable
    }

}