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
        System.out.println(encoder.encode("password")); // Aqui... Troque a senha 123456 pela senha que você deseja

        System.out.println();

        System.out.println(encoder.matches(
                "password",
                "$2a$10$5JFcc863HRcftf3QFE8uc.kCc5XVRCGxmSY2MoR8JsyLg0dpPTIRa"
        ));
    }
}
