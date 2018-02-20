# Distribuiti

## Database Distribuito
Realizzare un filesystem distribuito. Il filesystem deve avere una directory globale sempre consistente (non EVENTUALLY consistent).
I files sono inizialmente scritti solo sul nodo che li genera. I files vengono replicati in modo lazy su richiesta di nodi diversi da quello dove il file ha avuto origine.
La consistenza del contenuto di un file deve essere garantita sempre anche a fronte di repliche.
Si consideri il problema dei diritti di accesso, si possono considerare i componenti del sistema trusted.
Le modifiche sono propagate alla chiusura del file (no write through).

### Progetto 3
Ogni singolo file creato viene replicato sin dall�inizio per garantire fault tolerance. La directory e� distribuita e replicata (in parte o completamente) per essere resistente alla sparizione di un nodo.

### Struttura generale
*Implementare la classe che raccoglie gli attributi dei file (File Handle, Name, Type, Location, Size, Protection, Time Date ad User identification) 
*Implementare le operazioni che implementa il filesystem 
*Implementare sincronizzazione per la gestione dei file 
*Implementare la cache per ogni node 
*Implementare autenticazione e sicurezza 
*Implementare interfaccia grafica per la gestione del FileSystem 