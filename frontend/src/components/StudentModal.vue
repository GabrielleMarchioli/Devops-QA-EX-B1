<script setup>
import { reactive } from 'vue'
import { X } from 'lucide-vue-next'

const emit = defineEmits(['close', 'save'])

const form = reactive({
  nome: '',
  cursosLiberados: 0,
  plano: 'BASICO',
  moedas: 0
})

function salvar() {
  if (!form.nome.trim()) {
    return
  }

  emit('save', {
    nome: form.nome.trim(),
    cursosLiberados: Number(form.cursosLiberados),
    plano: form.plano,
    moedas: Number(form.moedas)
  })
}
</script>

<template>
  <div class="modal-overlay" @click.self="emit('close')">
    <div class="modal-card">
      <div class="modal-header">
        <div>
          <h2>Novo aluno</h2>
          <p>Adicione um estudante à plataforma.</p>
        </div>

        <button class="close-button" @click="emit('close')">
          <X :size="22" />
        </button>
      </div>

      <form @submit.prevent="salvar">
        <div class="form-group">
          <label>Nome do aluno</label>

          <input
              v-model="form.nome"
              type="text"
              placeholder="Ex: Gabrielle Marchioli"
              required
          />
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Cursos liberados</label>

            <input
                v-model="form.cursosLiberados"
                type="number"
                min="0"
            />
          </div>

          <div class="form-group">
            <label>Moedas</label>

            <input
                v-model="form.moedas"
                type="number"
                min="0"
            />
          </div>
        </div>

        <div class="form-group">
          <label>Plano</label>

          <select v-model="form.plano">
            <option value="BASICO">Básico</option>
            <option value="PREMIUM">Premium</option>
          </select>
        </div>

        <div class="modal-actions">
          <button
              type="button"
              class="secondary-button"
              @click="emit('close')"
          >
            Cancelar
          </button>

          <button type="submit" class="primary-button">
            Cadastrar aluno
          </button>
        </div>
      </form>
    </div>
  </div>
</template>