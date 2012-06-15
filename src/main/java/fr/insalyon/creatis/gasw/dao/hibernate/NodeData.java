/* Copyright CNRS-CREATIS
 *
 * Rafael Silva
 * rafael.silva@creatis.insa-lyon.fr
 * http://www.rafaelsilva.com
 *
 * This software is a grid-enabled data-driven workflow manager and editor.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
package fr.insalyon.creatis.gasw.dao.hibernate;

import fr.insalyon.creatis.gasw.bean.Node;
import fr.insalyon.creatis.gasw.dao.DAOException;
import fr.insalyon.creatis.gasw.dao.NodeDAO;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 *
 * @author Rafael Silva
 */
public class NodeData implements NodeDAO {

    private static final Logger logger = Logger.getLogger(JobData.class);
    private SessionFactory sessionFactory;

    public NodeData(SessionFactory sessionFactory) {

        this.sessionFactory = sessionFactory;
    }

    @Override
    public void add(Node node) throws DAOException {
        
        try {
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            session.saveOrUpdate(node);
            session.getTransaction().commit();
            session.close();

        } catch (HibernateException ex) {
            logger.error(ex);
            throw new DAOException(ex);
        }
    }

    @Override
    public Node getNodeBySiteAndNodeName(String site, String nodeName) throws DAOException {
        
        try {
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            Node node = (Node) session.getNamedQuery("Node.findBySiteAndNodeName")
                    .setString("siteName", site)
                    .setString("nodeName", nodeName)
                    .uniqueResult();
            session.getTransaction().commit();
            session.close();

            return node;

        } catch (HibernateException ex) {
            logger.error(ex);
            throw new DAOException(ex);
        }
    }
}
