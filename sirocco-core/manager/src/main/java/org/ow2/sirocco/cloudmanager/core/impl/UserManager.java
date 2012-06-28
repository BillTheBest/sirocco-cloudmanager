/**
 *
 * SIROCCO
 * Copyright (C) 2011 France Telecom
 * Contact: sirocco@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  $Id$
 *
 */

package org.ow2.sirocco.cloudmanager.core.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.log4j.Logger;
import org.ow2.sirocco.cloudmanager.core.api.IRemoteUserManager;
import org.ow2.sirocco.cloudmanager.core.api.IUserManager;
import org.ow2.sirocco.cloudmanager.core.api.exception.CloudProviderException;
import org.ow2.sirocco.cloudmanager.core.utils.PasswordValidator;
import org.ow2.sirocco.cloudmanager.core.utils.UtilsForManagers;
import org.ow2.sirocco.cloudmanager.model.cimi.CloudEntryPoint;
import org.ow2.sirocco.cloudmanager.model.cimi.extension.User;

@Stateless
@Remote(IRemoteUserManager.class)
@Local(IUserManager.class)
@SuppressWarnings("unused")
public class UserManager implements IUserManager {

    private static Logger logger = Logger.getLogger(UserManager.class.getName());

    @PersistenceContext(unitName = "persistence-unit/main", type = PersistenceContextType.TRANSACTION)
    private EntityManager em;

    @Resource
    private SessionContext ctx;

    @Override
    public User createUser(final String firstName, final String lastName, final String email, final String username,
        final String password) throws CloudProviderException {
        User u = new User();
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        u.setUsername(username);
        u.setPassword(password);

        return this.createUser(u);

    }

    @Override
    public User createUser(final User u) throws CloudProviderException {
        // if (!isUserValid(u)) {
        // throw new UserException("user validation failed");
        // }
        u.setRole("sirocco-user");
        this.em.persist(u);
        CloudEntryPoint cep = new CloudEntryPoint();
        cep.setUser(u);
        this.em.persist(cep);
        return u;
    }

    private boolean isUserValid(final User u) {
        if (u.getFirstName() == null) {
            return false;
        }
        if (u.getFirstName().equals("")) {
            return false;
        }

        if (u.getLastName() == null) {
            return false;
        }
        if (u.getLastName().equals("")) {
            return false;
        }

        if (u.getEmail() == null) {
            return false;
        }
        if (!(EmailValidator.getInstance().isValid(u.getEmail()))) {
            return false;
        }

        if (u.getPassword() == null) {
            return false;
        }
        if (!(new PasswordValidator().validate(u.getPassword()))) {
            return false;
        }

        return true;
    }

    @Override
    public User getUserById(final String userId) throws CloudProviderException {

        User result = this.em.find(User.class, new Integer(userId));

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<User> getUsers() throws CloudProviderException {
        return (List<User>)this.em.createQuery("Select u From User u").getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public User getUserByUsername(final String userName) throws CloudProviderException {

        User u = null;

        List<User> l = this.em.createQuery("FROM User u WHERE u.username=:usrname").setParameter("usrname", userName)
            .getResultList();

        if (!l.isEmpty()) {
            return l.get(0);
        } else {
            UserManager.logger.info("User " + userName + " unknown");
            return null;
        }
    }

    @Override
    public User updateUser(final String id, final Map<String, Object> updatedAttributes) throws CloudProviderException {

        User u = this.getUserById(id);

        try {
            UtilsForManagers.fillObject(u, updatedAttributes);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CloudProviderException();
        }

        return this.updateUser(u);
    }

    @Override
    public User updateUser(final User user) throws CloudProviderException {

        Integer userId = user.getId();
        // if (!isUserValid(user)) {
        // throw new UserException("user validation failed");
        // }
        this.em.merge(user);

        return user;
    }

    @Override
    public void deleteUser(final String userId) throws CloudProviderException {

        User result = this.getUserById(userId);

        if (result != null) {
            this.em.remove(result);
        }

    }

}
