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
 *  $Id: CloudProviderLocation.java 897 2012-02-15 10:13:25Z ycas7461 $
 *
 */

package org.ow2.sirocco.cloudmanager.model.cimi;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;


@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = {"iso3166_1", "iso3166_2", "postal_code"}), @UniqueConstraint(columnNames ={"gps_latitude", "gps_longitude", "gps_altitude"})})
public class CloudProviderLocation implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Integer id;

    /**
     * Iso3166-1 alpha-2 code (country code)
     *  -example: FR for France
     */  
    private String Iso3166_1;
    /**
     * Iso3166-2 code (subdivision code/province/departement/district, etc.)
     * - example:for France, FR-M (Lorraine), but we could also store FR-55 (Meuse)
     * which is more precise
     */
    private String Iso3166_2;
    
    /**
     * country specific postal Code (or City code for countries without any
     * postal code like Ireland).
     * This code is alphanumerical and has no standard.
     */
    private String postalCode;

    /**
     * country name matching iso 3166-1
     */    
    private String countryName;
    
    /**
     * state name matching iso 3166-2
     */
    private String stateName;
    
    /**
     * city name (for informational purposes only)
     */    
    private String cityName;
    
    /**
     * GPS Latitude
     */    
    private Double GPS_Latitude;
    
    /**
     * GPS Longitude
     */    
    private Double GPS_Longitude;
    
    /**
     * GPS Altitude
     */    
    private Double GPS_Altitude;
    
    private Set<CloudProvider> cloudProviders;
    
    @Column(name="iso3166_1")
    public String getIso3166_1() {
        return Iso3166_1;
    }

    public void setIso3166_1(String iso3166_1) {
        Iso3166_1 = iso3166_1;
    }

    @Column(name="iso3166_2")
    public String getIso3166_2() {
        return Iso3166_2;
    }

    public void setIso3166_2(String iso3166_2) {
        Iso3166_2 = iso3166_2;
    }

    @Column(name="postal_code")
    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    @Column(name="gps_latitude")
    public Double getGPS_Latitude() {
        return GPS_Latitude;
    }

    public void setGPS_Latitude(Double gPS_Latitude) {
        GPS_Latitude = gPS_Latitude;
    }

    @Column(name="gps_longitude")
    public Double getGPS_Longitude() {
        return GPS_Longitude;
    }

    public void setGPS_Longitude(Double gPS_Longitude) {
        GPS_Longitude = gPS_Longitude;
    }

    @Column(name="gps_altitude")
    public Double getGPS_Altitude() {
        return GPS_Altitude;
    }

    public void setGPS_Altitude(Double gPS_Altitude) {
        GPS_Altitude = gPS_Altitude;
    }

    public String getCountryName() {
        return this.countryName;
    }

    public void setCountryName(final String countryName) {
        this.countryName = countryName;
    }

    public String getStateName() {
        return this.stateName;
    }

    public void setStateName(final String stateName) {
        this.stateName = stateName;
    }

    public CloudProviderLocation() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer getId() {
        return this.id;
    }

    public void setId(final Integer Id) {
        this.id = Id;
    }

    @ManyToMany
	public Set<CloudProvider> getCloudProviders() {
		return cloudProviders;
	}

	public void setCloudProviders(Set<CloudProvider> cloudProviders) {
		this.cloudProviders = cloudProviders;
	}

}
