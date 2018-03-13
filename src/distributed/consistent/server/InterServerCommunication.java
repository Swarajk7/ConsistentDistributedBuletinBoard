package distributed.consistent.server;

import distributed.consistent.database.ArticleRepository;
import distributed.consistent.server.interfaces.IInterServerCommunication;

import java.rmi.RemoteException;

public class InterServerCommunication implements IInterServerCommunication {
    @Override
    public void joinMainServer(String rmi_registry_address, String rmi_binding_name, int portnum) throws RemoteException {
        try {
            ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
            serverInfoRepository.addServerAddress(rmi_registry_address, portnum, rmi_binding_name);
        } catch (Exception ex) {
            throw new RemoteException(ex.getMessage());
        }
    }

    @Override
    public void leaveMainServer(String rmi_registry_address, String rmi_binding_name, int portnum) throws RemoteException {
        try {
            ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
            serverInfoRepository.removeServerAddress(rmi_registry_address, portnum, rmi_binding_name);
        } catch (Exception ex) {
            throw new RemoteException(ex.getMessage());
        }
    }

    public void PostArticleAtMainServer(String content) throws RemoteException {
        try {
            ArticleRepository repository = new ArticleRepository(ConfigManager.DATABASE_FILE_PATH);
            int generatedArticleId = repository.WriteArticleAndGenerateID(content);
            //tell other servers to update the data at their side asynchronously
            //by queuing and calling their endpoint synchornously
            //mean while client can poll the original replica for status update
        } catch (Exception ex) {
            throw new RemoteException(ex.getMessage());
        }
    }
}
