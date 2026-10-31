package com.mais.seguradora.utils;

public class DocumentUtil {

    public static String cpfMascara(String cpf){

        if (cpf == null){
            return "";
        }

        String numeros = cpf.replaceAll("\\D", "");

        if(numeros.length() != 11 ){
            return cpf;
        }

        return "•••." + numeros.substring(3, 6) + ".•••-••";
    }
}
