import React from 'react';
import {Element} from 'nav-frontend-typografi'
import {ReactComponent as Logo} from './nav-logo.svg';
import {Page} from "../../hooks/use-routing";
import './header.less';

function Header() {
    return (
        <div className="application__header header">
            <Logo className="header__logo"/>
            <Element tag="h1" className="header__appnavn">Modiapersonoversikt - Skrivestøtte admin</Element>
            <a href={Page.REDIGER} className="header__lenke">
                Rediger
            </a>
            <a href={Page.STATISTIKK} className="header__lenke">
                Statistikk
            </a>
        </div>
    );
}

export default Header;
