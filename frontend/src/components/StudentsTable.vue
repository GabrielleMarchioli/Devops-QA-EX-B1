<script setup>
import { Trash2 } from 'lucide-vue-next'

defineProps({
  alunos: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['delete'])
</script>

<template>
  <div class="table-card">
    <div class="table-header">
      <div>
        <h2>Alunos cadastrados</h2>
        <p>Gerencie os alunos da plataforma</p>
      </div>
    </div>

    <div v-if="alunos.length === 0" class="empty-state">
      Nenhum aluno cadastrado ainda.
    </div>

    <div v-else class="table-wrapper">
      <table>
        <thead>
        <tr>
          <th>ID</th>
          <th>Aluno</th>
          <th>Cursos liberados</th>
          <th>Plano</th>
          <th>Moedas</th>
          <th></th>
        </tr>
        </thead>

        <tbody>
        <tr v-for="aluno in alunos" :key="aluno.id">
          <td>#{{ aluno.id }}</td>

          <td>
            <div class="student-info">
              <div class="student-avatar">
                {{ aluno.nome?.charAt(0)?.toUpperCase() }}
              </div>

              <div>
                <strong>{{ aluno.nome }}</strong>
                <span>Aluno ativo</span>
              </div>
            </div>
          </td>

          <td>
              <span class="course-count">
                {{ aluno.cursosLiberados }}
              </span>
          </td>

          <td>
              <span
                  class="plan-badge"
                  :class="aluno.plano?.toLowerCase()"
              >
                {{ aluno.plano }}
              </span>
          </td>

          <td>
            {{ aluno.moedas }}
          </td>

          <td class="actions">
            <button
                class="delete-button"
                @click="emit('delete', aluno.id)"
                title="Excluir aluno"
            >
              <Trash2 :size="18" />
            </button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>