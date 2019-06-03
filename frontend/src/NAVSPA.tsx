import * as React from 'react';
import * as ReactDOM from 'react-dom';
import {TextAlignProperty} from "csstype";

const getWindow = () => (window as any);
type NAVSPAApp = (element: HTMLElement, props: any) => void;

interface NAVSPAScope {
    [name: string]: NAVSPAApp;
}

interface State {
    hasError: boolean;
}

const feilmeldingstyling = {
    maxWidth: '400px',
    margin: '0 auto',
    padding: '2rem',
    textAlign: "center" as TextAlignProperty,
    backgroundColor: '#BA3A26',
    color: 'white',
    fontSize: '2rem'
};

export default class NAVSPA {
    public static eksporter<PROPS>(name: string, component: React.ComponentType<PROPS>) {
        NAVSPA.scope[name] = (element: HTMLElement, props: PROPS) => {
            ReactDOM.render(React.createElement(component, props), element);
        };
    }

    public static importer<PROPS>(name: string): React.ComponentType<PROPS> {
        class NAVSPAImporter extends React.Component<PROPS, State> {

            private el?: HTMLElement;

            constructor(props: PROPS) {
                super(props);
                this.state = {
                    hasError: false,
                };
            }

            public componentDidCatch(error: Error) {
                this.setState({hasError: true});
                getWindow().frontendlogger.error(error);
            }

            public componentDidMount() {
                try {
                    if (this.el) {
                        NAVSPA.scope[name](this.el, this.props);
                    }
                } catch (e) {
                    this.setState({hasError: true});
                    getWindow().frontendlogger.error(e);
                }
            }

            public componentWillUnmount() {
                if (this.el) {
                    ReactDOM.unmountComponentAtNode(this.el);
                }
            }

            public render() {
                if (this.state.hasError) {
                    return (
                        <div style={feilmeldingstyling}>
                            Feil i {name}
                        </div>
                    );
                }
                return <div ref={this.saveRef}/>;
            }

            private saveRef = (el: HTMLDivElement) => {
                this.el = el;
            };
        }

        return NAVSPAImporter;
    }

    public static render<PROPS>(name: string, element: HTMLElement, props: PROPS): void {
        NAVSPA.scope[name](element, props);
    }

    private static scope: NAVSPAScope = (getWindow().NAVSPA = getWindow().NAVSPA || {});
}