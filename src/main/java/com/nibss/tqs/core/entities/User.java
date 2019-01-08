package com.nibss.tqs.core.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.nibss.tqs.core.repositories.IOrganization;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import static java.util.stream.Collectors.*;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Entity
@Table(name = "users")
@NamedQueries(
        {
                @NamedQuery(name = "User.findAdminUsersByOrganization",
                        query = "SELECT new com.nibss.tqs.core.entities.User(u.id,u.firstName,u.lastName,u.email, u.enabled) FROM User u, IN(u.roles) r WHERE u.organization.id=?1 AND r.name=?2"),

                @NamedQuery(name = "User.countUserRoleByOrganization",
                        query = "SELECT COUNT(u) FROM User u, IN(u.roles) r WHERE u.organization.id=?1 AND r.name=?2"),

                @NamedQuery(name = "User.findByOrganization",
                        query = "SELECT new com.nibss.tqs.core.entities.User(u.id,u.firstName,u.lastName,u.email, u.enabled) FROM User u WHERE u.organization.id = ?1")
        }
)
@ToString(of = {"email", "firstName", "lastName"})
public class User extends BaseEntity implements UserDetails {

    /**
     *
     */
    private static final long serialVersionUID = -1849950767316299000L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private int id;

    @Column(nullable = false, unique = true)
    @Getter
    @Setter
    @NotNull(message = "Kindly specify the Email")
    @Email(message = "Kindly provide a valid Email")
    private String email;

    @Column(nullable = false, length = 300)
    @Setter
    private String password;

    @Column(nullable = false)
    @Setter
    private boolean enabled;

    @Getter
    @Setter
    @Column(name = "first_name")
    @NotBlank(message = "Kindly provide a First Name")
    private String firstName;

    @Getter
    @Setter
    @Column(name = "last_name")
    @NotBlank(message = "Kindly provide a Last Name")
    private String lastName;

    @Getter
    @Setter
    @Column(name = "password_changed")
    private boolean passwordChanged;

    @Getter
    @Setter
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_login_date")
    private Date lastLoginDate;


    /**
     * after the user is authenticated, this field is set to the lastLoginDate field before lastLoginDate is updated
     * to reflect the present time
     */
    @Transient
    @Getter
    @Setter
    private Date previousLoginDate;

    @Getter
    @Setter
    @Column(name = "created_by")
    private String createdBy;


    @Transient
    List<SimpleGrantedAuthority> authorities = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    @Getter
    @Setter
    private Organization organization;


    @Column(name="organization_id", updatable = false, insertable = false)
    private int organizationId;

    @Column(name = "organization_id", nullable = false, updatable = false, insertable = false)
    private int organizationType;

    @Transient
    @Getter
    private IOrganization organizationInterface;

    public void setOrganizationInterface(IOrganization organizationInterface) {
        this.organizationInterface = organizationInterface;
        initRoles();

    }

    @Transient
    @Setter
    private boolean accountNonExpired = true;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST, mappedBy = "users")
    @OrderBy("name ASC")
    @Getter
    @Setter
    private List<Role> roles = new ArrayList<>(0);

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        if (authorities.isEmpty())
            initRoles();

        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public String getFullName() {
        return String.format("%s %s", lastName, firstName);
    }

 /*   private boolean isAdminUser(List<SimpleGrantedAuthority> authorities) {
        if (null == authorities)
            return false;

        return authorities.stream().anyMatch(r -> r.getAuthority().equals(Role.ADMIN));
    }*/

    protected void initRoles() {
//        boolean isAdmin = false;
        if (roles != null && !roles.isEmpty()) {
            authorities = roles.stream().map(r -> new SimpleGrantedAuthority(r.getName())).collect(toList());
        }
        if (organizationType == Integer.parseInt(OrganizationType.NIBSS)) {
            if (null != authorities) {
                if (authorities.stream().anyMatch(r -> r.getAuthority().equals(Role.ADMIN)))
                    authorities.add(new SimpleGrantedAuthority(Role.NIBSS_ADMIN));
                else
                    authorities.add(new SimpleGrantedAuthority(Role.NIBSS_USER));
            }
            authorities.add( new SimpleGrantedAuthority(Role.NIBSS));
        } else if (organizationType == Integer.parseInt(OrganizationType.BANK)) {
            if (null != authorities) {
                if (authorities.stream().anyMatch(r -> r.getAuthority().equals(Role.ADMIN)))
                    authorities.add(new SimpleGrantedAuthority(Role.BANK_ADMIN));
                else
                    authorities.add(new SimpleGrantedAuthority(Role.BANK_USER));
            }
            authorities.add(new SimpleGrantedAuthority(Role.BANK));
        } else if (organizationType == Integer.parseInt(OrganizationType.AGGREGATOR)) {
            SimpleGrantedAuthority aggre = new SimpleGrantedAuthority("ROLE_AGGREGATOR");
            if (null != authorities)
                authorities.add(aggre);
        } else if (organizationType == Integer.parseInt(OrganizationType.MERCHANT)) {
            SimpleGrantedAuthority merchant = new SimpleGrantedAuthority("ROLE_MERCHANT");
            if (null != authorities)
                authorities.add(merchant);
        }

    }

    public User() {
    }

    public User(User user, int organizationType) {
        this.organizationType = organizationType;

        this.roles = user.roles;
        this.firstName = user.firstName;
        this.lastName = user.lastName;
        this.lastLoginDate = user.lastLoginDate;
        this.previousLoginDate = user.lastLoginDate;
        this.id = user.id;

        this.email = user.email;

        this.enabled = user.enabled;
        this.password = user.password;
        this.passwordChanged = user.passwordChanged;
        this.dateCreated = user.dateCreated;
    }

    public User(int id,String firstName, String lastName, String email, boolean enabled) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.enabled = enabled;
    }
}
