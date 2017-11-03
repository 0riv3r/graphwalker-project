import React, { Component } from 'react';
import { Layout } from 'antd';
import { StudioHeader, GraphEditor, StudioMenu } from "../../components";
import './styles.less';

const { Sider } = Layout;

export default class Studio extends Component {

  constructor(props) {
    super(props);
    this.state = {
      collapsed: true,
    };
  }

  toggle() {
    this.setState({
      collapsed: !this.state.collapsed,
    });
  }

  render() {
    return (
      <Layout className="container">
        <Sider trigger={null} collapsible collapsed={this.state.collapsed}>
          <div className="logo" />
          <StudioMenu/>
        </Sider>
        <Layout className="content-container">
          <StudioHeader collapsed={this.state.collapsed} toggle={() => this.toggle()}/>
          <GraphEditor/>
        </Layout>
      </Layout>
    );
  }
}
