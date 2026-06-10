# Exportacao de arvores por data

## Contrato da API

```http
GET /api/arvores/exportacao?dataInicial=2026-01-01&dataFinal=2026-01-31&formato=csv
Authorization: Bearer <token>
```

Parametros:

- `dataInicial`: obrigatoria, formato ISO `yyyy-MM-dd`.
- `dataFinal`: obrigatoria, formato ISO `yyyy-MM-dd`.
- `formato`: obrigatorio, aceita `csv` ou `xlsx`, sem diferenca entre maiusculas e minusculas.

O intervalo inclui as duas datas. Internamente, a consulta usa `data_cadastro >= inicio`
e `data_cadastro < dia_seguinte_ao_fim`, evitando perder registros com horario no fim do dia.

Perfis autorizados: `ADMINISTRADOR`, `GESTOR` e `PESQUISADOR`.

Respostas:

- `200`: arquivo em streaming com `Content-Disposition: attachment`.
- `400`: data ausente ou fora do formato ISO.
- `401`: usuario nao autenticado.
- `403`: perfil sem permissao.
- `413`: intervalo excede o limite configurado.
- `422`: intervalo invertido ou formato de arquivo invalido.

O nome segue o padrao `arvores_2026-01-01_a_2026-01-31.csv`.

Arvores anteriores a migracao recebem a data da primeira coleta vinculada. Quando nao
existe coleta historica, recebem a data de execucao da migracao, pois o esquema antigo
nao armazenava uma data de cadastro confiavel.

## Colunas

O arquivo inclui:

`ID da arvore`, `Codigo`, `Especie`, `Nome comum`, `Bairro`, `Rua`, `Referencia`,
`Latitude`, `Longitude`, `Data de cadastro`, `Status`, `Altura (m)`, `DAP (cm)`,
`Copa (m)`, `Estado geral`, `Vigor` e `Observacoes`.

O CSV usa UTF-8 com BOM, separador `;`, campos entre aspas e neutralizacao de valores
textuais que poderiam ser interpretados como formulas por planilhas. O XLSX preserva
numeros e datas como tipos nativos.

## Fluxo de frontend

1. Exibir dois campos `input type="date"` e um seletor de formato.
2. Tornar os tres campos obrigatorios.
3. Bloquear o envio quando a data inicial for posterior a final.
4. Desabilitar o botao e mostrar progresso enquanto a requisicao estiver ativa.
5. Baixar o `Blob` retornado usando o nome de `Content-Disposition`.
6. Em erro JSON, apresentar a mensagem da API e reabilitar o botao.

Exemplo em JavaScript:

```js
async function exportarArvores({ dataInicial, dataFinal, formato, token }) {
  if (!dataInicial || !dataFinal || !formato) {
    throw new Error("Preencha o intervalo e o formato.");
  }
  if (dataInicial > dataFinal) {
    throw new Error("A data inicial nao pode ser posterior a data final.");
  }

  const params = new URLSearchParams({ dataInicial, dataFinal, formato });
  const response = await fetch(`/api/arvores/exportacao?${params}`, {
    headers: { Authorization: `Bearer ${token}` },
  });

  if (!response.ok) {
    const erro = await response.json().catch(() => null);
    throw new Error(erro?.message ?? "Nao foi possivel gerar a exportacao.");
  }

  const blob = await response.blob();
  const disposition = response.headers.get("Content-Disposition") ?? "";
  const match = disposition.match(/filename\*?=(?:UTF-8'')?"?([^";]+)/i);
  const fallback = `arvores_${dataInicial}_a_${dataFinal}.${formato.toLowerCase()}`;
  const nome = match ? decodeURIComponent(match[1]) : fallback;
  const url = URL.createObjectURL(blob);
  const link = Object.assign(document.createElement("a"), { href: url, download: nome });
  link.click();
  URL.revokeObjectURL(url);
}
```

Para o navegador conseguir ler `Content-Disposition` em uma origem diferente, a
configuracao CORS deve expor esse cabecalho.

## Performance e operacao

- A consulta usa uma projecao escalar: fotos e colecoes da entidade nao sao carregadas.
- Os dados sao lidos em lotes de 1.000 registros.
- O XLSX usa `SXSSFWorkbook`, mantendo apenas uma janela de linhas em memoria.
- O indice `(data_cadastro, id)` atende ao filtro e a ordenacao estavel.
- O limite padrao e 100.000 registros e pode ser alterado por
  `ARBOR_EXPORTACAO_MAX_REGISTROS`.
- Exportacoes maiores devem ser tratadas como jobs assincronos, armazenadas fora da
  aplicacao e disponibilizadas por URL temporaria com expiracao.

Arquivos exportados nao devem ser gravados permanentemente no servidor. Logs devem
registrar usuario, intervalo, formato e quantidade, mas nunca tokens ou o conteudo do
arquivo.
