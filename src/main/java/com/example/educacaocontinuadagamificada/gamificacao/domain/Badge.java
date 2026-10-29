package com.example.educacaocontinuadagamificada.gamificacao.domain;

/** Conquistas concedidas na conclusao de cursos. Cada badge conhece o proprio criterio. */
public enum Badge {

    PRIMEIRA_CONCLUSAO {
        @Override
        boolean conquistada(int cursosConcluidos, double media) {
            return cursosConcluidos >= 1;
        }
    },
    EXCELENCIA {
        @Override
        boolean conquistada(int cursosConcluidos, double media) {
            return media >= 9.0;
        }
    },
    MARATONISTA {
        @Override
        boolean conquistada(int cursosConcluidos, double media) {
            return cursosConcluidos >= 5;
        }
    };

    abstract boolean conquistada(int cursosConcluidos, double media);
}
