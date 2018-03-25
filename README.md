# Distribuiti

## Database Distribuito
Realizzare un filesystem distribuito. Il filesystem deve avere una directory globale sempre consistente (non EVENTUALLY consistent).
I files sono inizialmente scritti solo sul nodo che li genera. I files vengono replicati in modo lazy su richiesta di nodi diversi da quello dove il file ha avuto origine.
La consistenza del contenuto di un file deve essere garantita sempre anche a fronte di repliche.
Si consideri il problema dei diritti di accesso, si possono considerare i componenti del sistema trusted.
Le modifiche sono propagate alla chiusura del file (no write through).

### Progetto 3
Ogni singolo file creato viene replicato sin dall’inizio per garantire fault tolerance. La directory e’ distribuita e replicata (in parte o completamente) per essere resistente alla sparizione di un nodo.

### Struttura generale
* Implementare la classe che raccoglie gli attributi dei file (File Handle, Name, Type, Location, Size, Protection, Time Date ad User identification) 
* Implementare le operazioni che implementano il filesystem 
* Implementare sincronizzazione per la gestione dei file (Gestione della concorrenza) 
* Implementare la cache per ogni nodo 
* Implementare autenticazione e sicurezza 
* Gestire la replicazione dei file (il file viene salvato nel PC di chi salva e nel PC che ha meno spazio utilizzato)
* Gestire l'aggiunta di un nuovo nodo, dopo un tot di tempo che è collegato, lo usiamo per la replicazione
* Gestire l'uscita del nodo, ogni tot tempo ogni nodo controlla se c'è almeno una replica per ogni file
* Se un nodo rientra, controlla se i propri file sono aggiornati alla versione più recente
* Implementare interfaccia grafica per la gestione del FileSystem 


