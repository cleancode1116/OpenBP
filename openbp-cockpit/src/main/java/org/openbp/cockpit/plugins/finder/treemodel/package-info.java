/** 
Model for a tree that is dynamically created. All that is need
is a strategy for the generic structure and various mapper
for the nodes.

The strategy contains the method createDataMapper. This data mapper
is responsible to map the object that should be represented by the
tree to the structure of the tree. 

E.g. if you want to display various books in the tree and the
books should be grouped by the publisher and the author like this

@code
 |-- publisher 1
 |    |- author 1
 |    |   | title 1
 |    |   | title 2
 |    |- author 2
 |    |   | title 3
 |-- publisher 2
 .    |- author 3
 .    |   | title 4
 .    |   | title 5
 .    |- author 4
 .        | title 6
@code

The datamapper extractes the information like the publisher, the
author and the title from the book. He creates the nodes for the 
publisher and the author as a PropertyNode and the title as LeafNode. 
All nodes can contain any object as node data. The NodeMapper maps 
the node data to a string that is displayed in the tree. Each node 
can have a different node mapper depending on the content of the
node. The mapper must be set, when the node is created either by the
createLeafNode or createPropertyNode method.
 */

package org.openbp.cockpit.plugins.finder.treemodel;
