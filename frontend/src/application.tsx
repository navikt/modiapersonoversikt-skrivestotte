import React from 'react';
import classNames from "classnames";
import Header from "./components/header/header";
import RedigerVisning from "./sider/redigering/visning";
import StatistikkVisning from "./sider/statistikk/visning";
import AdminVisning from "./sider/admin/visning";
import useRouting, {Page} from "./hooks/use-routing";
import './nav-frontend/nav-frontend.css';
import './application.scss';

interface Props {
    renderHead: boolean;
}

function contentCls(page: Page): string {
    return classNames('application__content', `${page.slice(1)}-page`);
}
const pageComponent: { [key: string ]: React.ComponentType } = {
    [Page.REDIGER]: RedigerVisning,
    [Page.STATISTIKK]: StatistikkVisning,
    [Page.ADMIN]: AdminVisning
}

function Application(props: Props) {
    const page = useRouting();
    const visning = pageComponent[page];
    return (
        <div className="application">
            {props.renderHead && <Header/>}
            <div className={contentCls(page)}>
                { React.createElement(visning) }
            </div>
        </div>
    );
}

export default Application;
