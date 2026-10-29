const API_URL = '/api/alunos'

export async function listarAlunos() {
    const response = await fetch(API_URL)

    if (!response.ok) {
        throw new Error('Não foi possível carregar os alunos.')
    }

    return response.json()
}

export async function criarAluno(aluno) {
    const response = await fetch(API_URL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(aluno)
    })

    if (!response.ok) {
        throw new Error('Não foi possível cadastrar o aluno.')
    }

    return response.json()
}

export async function excluirAluno(id) {
    const response = await fetch(`${API_URL}/${id}`, {
        method: 'DELETE'
    })

    if (!response.ok) {
        throw new Error('Não foi possível excluir o aluno.')
    }
}