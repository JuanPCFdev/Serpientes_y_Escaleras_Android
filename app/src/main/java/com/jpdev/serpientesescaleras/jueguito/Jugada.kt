package com.jpdev.serpientesescaleras.jueguito

class Jugada {
    var Dado1:Int = 0
    var Dado2:Int = 0
    var Suma:Int = 0;

    constructor(Dado1:Int,Dado2:Int) {
        this.Dado1 = Dado1
        this.Dado2 = Dado2
        this.Suma = Dado1 + Dado2
    }
}