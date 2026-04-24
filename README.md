# 🚀 Project: The "Bureaucracy Dodger" (Batch Utility Tool)

### 🧐 O que é isso?
Sabe aquele momento em que a "segurança corporativa" decide que você não pode mais rodar seus scripts úteis porque eles não têm uma assinatura digital que leva 3 meses e 42 aprovações para sair? Pois é. 

Este projeto é um protótipo de uma interface Swing clássica (com aquele cheirinho de Java de 2004) criado para servir de "casca" para ativação de processos via linha de comando. É o legítimo "se não tem cão, caça com gato".

### 🛠️ Por que Java Swing?
- Porque o Java já está instalado e "abençoado" pelos deuses do TI.
- Porque funciona.
- Porque o visual retrô afasta curiosos que buscam modernidades da Web 3.0.
- Porque sim.

### 🎨 Funcionalidades (ou quase isso)
- **Visual "Vintage":** Layout inspirado nos melhores instaladores de driver de impressora dos anos 90.
- **Console Dark Mode:** Para você se sentir um hacker enquanto apenas lê logs de um job que provavelmente vai dar erro.
- **Date Picker Americano:** Porque o sistema espera `MM/dd/yyyy` e a gente não quer brigar com o `Locale` da máquina.
- **Botão de Browse:** Para não ter que digitar caminhos gigantescos no Windows e errar a barra invertida.

### 🚀 Como rodar (sem frescura)
1. Certifique-se de que o Maven não está bloqueado pelo firewall (boa sorte).
2. Compile o monstro:
   ```bash
   mvn clean package
   ```
3. Execute o JAR maroto:
   ```bash
   java -jar target/internal-batch-tool-1.0-SNAPSHOT.jar
   ```

### Última dica de "mestre":
Quando você for criar o atalho no Windows para os usuários, no campo "Destino", use:
`javaw -jar "C:\Caminho\Para\Seu\Arquivo.jar"`

O `javaw` (com o 'w' no final) é o segredo para a aplicação abrir a janela bonitinha e não ficar com aquela janela preta do CMD pendurada na barra de tarefas.