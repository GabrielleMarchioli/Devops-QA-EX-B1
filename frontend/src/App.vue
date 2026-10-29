<script setup>
import { computed, onMounted, ref } from 'vue'

import {
  Users,
  BookOpen,
  Crown,
  Coins,
  Plus,
  RefreshCw
} from 'lucide-vue-next'

import Sidebar from './components/Sidebar.vue'
import MetricCard from './components/MetricCard.vue'
import StudentsTable from './components/StudentsTable.vue'
import StudentModal from './components/StudentModal.vue'

import {
  listarAlunos,
  criarAluno,
  excluirAluno
} from './services/alunoService'

const alunos = ref([])
const carregando = ref(false)
const erro = ref('')
const mensagem = ref('')
const modalAberto = ref(false)

const totalAlunos = computed(() => alunos.value.length)

const totalCursos = computed(() =>
    alunos.value.reduce(
        (total, aluno) => total + (aluno.cursosLiberados || 0),
        0
    )
)

const totalPremium = computed(() =>
    alunos.value.filter(
        aluno => aluno.plano?.toUpperCase() === 'PREMIUM'
    ).length
)

const totalMoedas = computed(() =>
    alunos.value.reduce(
        (total, aluno) => total + (aluno.moedas || 0),
        0
    )
)

async function carregarAlunos() {
  carregando.value = true
  erro.value = ''

  try {
    alunos.value = await listarAlunos()
  } catch (e) {
    erro.value = e.message
  } finally {
    carregando.value = false
  }
}

async function cadastrarAluno(aluno) {
  try {
    await criarAluno(aluno)

    modalAberto.value = false

    mensagem.value = 'Aluno cadastrado com sucesso.'

    await carregarAlunos()

    limparMensagem()
  } catch (e) {
    erro.value = e.message
  }
}

async function deletarAluno(id) {
  const confirmar = window.confirm(
      'Deseja realmente excluir este aluno?'
  )

  if (!confirmar) {
    return
  }

  try {
    await excluirAluno(id)

    mensagem.value = 'Aluno excluído com sucesso.'

    await carregarAlunos()

    limparMensagem()
  } catch (e) {
    erro.value = e.message
  }
}

function limparMensagem() {
  setTimeout(() => {
    mensagem.value = ''
  }, 3000)
}

onMounted(carregarAlunos)
</script>

<template>
  <div class="app-layout">
    <Sidebar />

    <main class="main-content">
      <header class="topbar">
        <div>
          <span class="page-kicker">VISÃO GERAL</span>
          <h1>Dashboard</h1>
          <p>
            Acompanhe alunos, cursos e recompensas da plataforma.
          </p>
        </div>

        <button
            class="primary-button new-student-button"
            @click="modalAberto = true"
        >
          <Plus :size="19" />
          Novo aluno
        </button>
      </header>

      <div v-if="erro" class="alert error-alert">
        {{ erro }}
      </div>

      <div v-if="mensagem" class="alert success-alert">
        {{ mensagem }}
      </div>

      <section class="metrics-grid">
        <MetricCard
            title="Total de alunos"
            :value="totalAlunos"
            subtitle="Estudantes cadastrados"
            :icon="Users"
        />

        <MetricCard
            title="Cursos liberados"
            :value="totalCursos"
            subtitle="Total disponível"
            :icon="BookOpen"
        />

        <MetricCard
            title="Alunos Premium"
            :value="totalPremium"
            subtitle="Plano avançado"
            :icon="Crown"
        />

        <MetricCard
            title="Moedas"
            :value="totalMoedas"
            subtitle="Recompensas acumuladas"
            :icon="Coins"
        />
      </section>

      <section v-if="carregando" class="loading-card">
        <RefreshCw class="spin" :size="24" />
        Carregando alunos...
      </section>

      <StudentsTable
          v-else
          :alunos="alunos"
          @delete="deletarAluno"
      />
    </main>

    <StudentModal
        v-if="modalAberto"
        @close="modalAberto = false"
        @save="cadastrarAluno"
    />
  </div>
</template>