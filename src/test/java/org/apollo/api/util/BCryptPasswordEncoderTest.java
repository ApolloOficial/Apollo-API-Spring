package org.apollo.api.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

    /**
     * Para usar essa classe e rodá-la, siga os passos abaixo:
        * Primeiramente, vá nas configurações de Run
        * Em seguida, clique em Edit Configurations
        * Depois, clique no mais ao lado esquerdo superior e selecione a primeira opção (Application)
        * Nomeie como quiser e no campo de baixo do Java SDK clique no símbolo de browse
        * Espere ele sugerir as classes e escolha a classe para ser executada (BCryptPasswordEncoderTest.java)
        * Clique em OK e pronto, só rodar com a senha desejada!
    */

public class BCryptPasswordEncoderTest {
    public static void main(String[] args) {
        // Para hashear uma senha, troque a senha pré-definida pela senha desejada
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("123456")); // Aqui... Troque a senha 123456 pela senha que você deseja
    }
}
