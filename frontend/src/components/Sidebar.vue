<script setup>
import {
  LayoutDashboard,
  Users,
  GraduationCap,
  Award
} from 'lucide-vue-next'
</script>

<template>
  <aside class="sidebar">
    <div class="brand">
      <div class="brand-icon">
        <GraduationCap :size="26" />
      </div>

      <div>
        <h1>Educa+</h1>
        <span>Learning Platform</span>
      </div>
    </div>

    <nav class="menu">
      <p class="menu-title">MENU</p>

      <a class="menu-item active">
        <LayoutDashboard :size="20" />
        <span>Dashboard</span>
      </a>

      <a class="menu-item">
        <Users :size="20" />
        <span>Alunos</span>
      </a>

      <a class="menu-item">
        <GraduationCap :size="20" />
        <span>Cursos</span>
      </a>

      <a class="menu-item">
        <Award :size="20" />
        <span>Recompensas</span>
      </a>
    </nav>

    <div class="sidebar-footer">
      <div class="avatar">GM</div>

      <div>
        <strong>Gabrielle</strong>
        <span>Administrador</span>
      </div>
    </div>
  </aside>
</template>